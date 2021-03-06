package com.spectralogic.dsl.models

import com.google.common.base.Function
import com.google.common.collect.FluentIterable
import com.spectralogic.ds3client.commands.HeadBucketRequest
import com.spectralogic.ds3client.commands.HeadBucketResponse
import com.spectralogic.ds3client.helpers.FileObjectGetter
import com.spectralogic.dsl.exceptions.BpException
import com.spectralogic.ds3client.helpers.channelbuilders.PrefixAdderObjectChannelBuilder
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers
import com.spectralogic.ds3client.helpers.FileObjectPutter
import com.spectralogic.ds3client.models.bulk.Ds3Object
import com.spectralogic.ds3client.commands.DeleteBucketRequest
import com.spectralogic.ds3client.commands.DeleteObjectsRequest
import com.spectralogic.dsl.helpers.Globals
import com.spectralogic.dsl.utils.BpObjectIterable
import com.spectralogic.dsl.utils.IteratorWrapper

import javax.annotation.Nullable
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/** Represents a BlackPearl getBucket */
class BpBucket {
    private final BpClient client
    private final Ds3ClientHelpers helper
    final String name
    final Long size
    final boolean empty
    final boolean exists

    BpBucket(String name, BpClient client) {
        this.client = client
        this.helper = Ds3ClientHelpers.wrap(client)
        this.name = name

        this.size = -1
        this.empty = true
        this.exists = false
    }

    /** Deletes this bucket */
    void delete() {
        client.deleteBucket(new DeleteBucketRequest(this.name))
    }

    /** Deletes all objects in this bucket */
    void deleteAllObjects() {
        def objIterator = new BpObjectIterable(this.client, this).iterator()
        while (objIterator.hasNext()) {
            def currBatch = objIterator.nextPage().collect { it.name }
            client.deleteObjects(new DeleteObjectsRequest(this.name, currBatch))

            /* reset iterator because deleting objects ruins paging */
            objIterator = new BpObjectIterable(this.client, this).iterator()
        }
    }

    /** Recursively delete objects in max 1000 object bulks */
    void deleteObjects(Iterable<BpObject> objects) {
        def objIterator = objects.iterator()
        while (objIterator.hasNext()) {
            def currentBatch = objIterator.take(Globals.OBJECT_PAGE_SIZE).collect { it.name }
            client.deleteObjects(new DeleteObjectsRequest(this.name, currentBatch))
        }
    }

    void deleteObject(BpObject object) {
        deleteObjects([object])
    }

    void deleteObject(String objectName) {
        deleteObjects([new BpObject(objectName, this, this.client)])
    }

    /**
     * @param pathStrs Directories and files to upload
     * @param remoteDir Remote directory on BP to put to
     * A specific file will be uploaded to the remote directory (or root directory if not specified)
     * A directory will have its file(s) uploaded to the remote directory (or root directory if not specified)
     *  all subdirectory files will also be uploaded with maintained directory structure.
     *  TODO: what happens when given absolute path or '..'
     */
    void putBulk(Iterable<String> pathStrs, String remoteDir='') {
        if (remoteDir.length() > 0) {
            remoteDir = remoteDir.endsWith('/') ? remoteDir : remoteDir + '/'
        }

        /* Ensure paths are absolute & exist */
        def paths = pathStrs.collect { pathStr ->
            def path = Paths.get(pathStr)
            path = path.absolute ? path : Paths.get("$Globals.HOME_DIR/$path")

            if (!Files.exists(path)) {
                throw new FileNotFoundException(path.toString())
            }

            return path
        }

        def pathGroups = paths.groupBy { Files.isDirectory(it) }
        def dirs = pathGroups[true] ?: []
        def files = pathGroups[false] ?: []

        /* grouped files */
        files.groupBy { it.getParent() }.each { parentDir, fileGroup ->
            def groupIterator = fileGroup.iterator()
            while (groupIterator) {
                def objs = groupIterator.take(Globals.MAX_BULK_LOAD).collect {
                    new Ds3Object(it.getFileName().toString(), Files.size(it))
                }

                def job = helper.startWriteJob(name, helper.addPrefixToDs3ObjectsList(objs, remoteDir))
                job.transfer(new PrefixAdderObjectChannelBuilder(new FileObjectPutter(parentDir), remoteDir))
            }
        }

        /* directories */
        dirs.each { dir ->
            def objIterator = helper.listObjectsForDirectory(dir).iterator()
            while (objIterator) {
                def objs = objIterator.take(Globals.MAX_BULK_LOAD).findAll { obj ->
                    !Files.isDirectory(Paths.get(dir.toString(), obj.name))
                }

                def job = helper.startWriteJob(name, helper.addPrefixToDs3ObjectsList(objs, remoteDir))
                job.transfer(new PrefixAdderObjectChannelBuilder(new FileObjectPutter(dir), remoteDir))
            }
        }
    }

    void putBulk(String pathStr, String remoteDir = '') {
        putBulk([pathStr], remoteDir)
    }

    /* raw get bulk method logic */
    private getJob(Iterator<String> iterator, Path destinationPath) {
        def objectNamesIter = new IteratorWrapper<String>(iterator)
        FluentIterable<String> ds3Objects = FluentIterable.from(objectNamesIter)

        def readJob = this.helper.startReadJob(this.name, ds3Objects.transform(new Function<String, Ds3Object>() {
            @Override
            Ds3Object apply(@Nullable String name) {
                return new Ds3Object(name)
            }
        }).toList())

        readJob.transfer(new FileObjectGetter(destinationPath))
    }

    /**
     * Writes objects to specified directory. Remote directories are specified by a trailing '/' on the object name.
     * Each object within that directory will be retrieved.
     * @param objectNames names of the objects to get
     * @param destinationPathStr directory to write to
     */
    void getBulk(Iterable<String> objectNames, String destinationPathStr='') {
        /* Make sure download directory isn't a file, create directory if it doesn't exist */
        def destinationPath = Paths.get(destinationPathStr)
        if (Files.isRegularFile(destinationPath)) {
            throw new BpException("$destinationPathStr is not a directory!")
        } else if (!Files.exists(destinationPath)) {
            Files.createDirectory(destinationPath)
        }

        /* Read objects, Globals.MAX_BULK_LOAD at a time. If directory is encountered, save directories for later */
        def nameIterator = objectNames.iterator()
        def directoryList = []
        while (nameIterator) {
            Map<Boolean,List<String>> batch = nameIterator.take(Globals.MAX_BULK_LOAD).collect().groupBy { String obj ->
                obj.endsWith('/')
            }

            if (batch[true]) directoryList.addAll(batch[true])
            if (batch[false]) this.getJob(batch[false].iterator(), destinationPath)
        }

        /* Search directory contents recursively */
        def searchDir
        searchDir = { String dir ->
            if (!dir) return []

            def dirList = this.helper.remoteListDirectory(this.name, dir)
            def dirs = dirList.findAll { it.isPrefix() }.collect { it.name }
            def files = dirList.findAll { it.isContents() }.collect { it.name }
            while (dirs) {
                files.addAll(searchDir(dirs.pop()))
            }

            return files
        }

        Iterator<String> dirObjects = directoryList.collect { searchDir(it) }.flatten().iterator()
        while (dirObjects) {
            this.getJob(dirObjects.take(Globals.MAX_BULK_LOAD), destinationPath)
        }
    }

    void getBulk(String objectName, String destinationPathStr='') {
        getBulk([objectName], destinationPathStr)
    }

    /** @return iterator for all BpObjects that uses pagination */
    Iterable<BpObject> getAllObjects() {
        return new BpObjectIterable(this.client, this)
    }

    /**
     * @param objectNames
     * @return iterator for BpObjects with given names
     */
    Iterable<BpObject> getObjects(Iterable<String> objectNames) {
        FluentIterable<String> names = FluentIterable.from(objectNames)
        def bucket = this
        def client = this.client
        return names.transform(new Function<String, BpObject>() {
            @Override
            BpObject apply(@Nullable String objectName) {
                return new BpObject(objectName, bucket, client)
            }
        })
    }

    BpObject getObject(String objectName) {
        return new BpObject(objectName, this, this.client)
    }

    /* object count */
    Long getSize() {
        return new BpObjectIterable(this.client, this).iterator().size()
    }

    boolean isExists() {
        switch (client.headBucket(new HeadBucketRequest(name)).status) {
            case HeadBucketResponse.Status.NOTAUTHORIZED:
            case HeadBucketResponse.Status.EXISTS:
                return true
            default:
                return false
        }
    }

    Boolean isEmpty() {
        return !(this.getAllObjects().iterator().hasNext())
    }

    String toString() {
        return "name: $name, client: {$client}"
    }

}
