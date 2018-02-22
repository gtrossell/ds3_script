package com.spectralogic.dsl.models

import com.spectralogic.ds3client.helpers.FileObjectGetter
import com.spectralogic.dsl.exceptions.BpException
import com.spectralogic.ds3client.helpers.channelbuilders.PrefixAdderObjectChannelBuilder
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers
import com.spectralogic.ds3client.helpers.FileObjectPutter
import com.spectralogic.ds3client.models.ListBucketResult
import com.spectralogic.ds3client.models.bulk.Ds3Object
import com.spectralogic.ds3client.commands.DeleteBucketRequest
import com.spectralogic.ds3client.commands.DeleteObjectsRequest
import com.spectralogic.ds3client.commands.GetBucketResponse
import com.spectralogic.dsl.helpers.Globals

import java.nio.file.Files
import java.nio.file.Paths

/** Represents a BlackPearl bucket, extended from GetBucketResponse */
class BpBucket {
    private final BpClient client
    private final helper
    private ListBucketResult listBucketResult
    final String name

    BpBucket(GetBucketResponse response, BpClient client) {
        this.client = client
        this.helper = Ds3ClientHelpers.wrap(client)
        this.listBucketResult = response.getListBucketResult()
        this.name = response.getListBucketResult().getName()
    }

    // TODO: don't return
    /** @return the current version of this bucket */
    BpBucket reload() {
        this.listBucketResult = client.bucket(this.name).listBucketResult
        return this
    }

    /**
     * Deletes bucket
     * Throws BpException if bucket is not empty so that data is not accidentally deleted
     */
    void delete() {
        if (this.objects()) {
            throw new BpException("Bucket must be empty to delete!")
        }
        client.deleteBucket(new DeleteBucketRequest(this.name))
    }

    /** Deletes all objects inside it */
    BpBucket deleteAllObjects() {
        return deleteObjects(*contentsToBpObjects(helper.listObjects(name)))
    }

    /** Recursively delete objects in max 1000 object bulks */
    BpBucket deleteObjects(BpObject... objects) {
        if (objects.size() < 1) return this

        def objIterator = objects.iterator()
        while (objIterator) {
            def currentBatch = objIterator.take(1_000)
            client.deleteObjects(new DeleteObjectsRequest(name, currentBatch.collect { it.name }))
        }

        return reload()
    }

    BpBucket deleteObjects(List<BpObject> objects) {
        return deleteObjects(*objects)
    }

    BpBucket deleteObject(BpObject object) {
        return deleteObjects([object])
    }

    /**
     * @param pathStrs Directories and files to upload
     * @param remoteDir Directory on BP to put to
     * Puts each file and each directory's files given into the bucket. Maintains the subdirectory
     * structure when putting directories.
     */
    BpBucket putBulk(List<String> pathStrs, String remoteDir = '') {
        def paths = pathStrs.collect { Paths.get(it) }

        /* ensure sure paths are absolute */
        paths = paths.collect { it.absolute ? it : Paths.get("$Globals.HOME_DIR/$it") }

        /* ensure paths exist */
        paths.each { path ->
            if (!Files.exists(path)) {
                throw new FileNotFoundException(path.toString())
            }
        }

        def pathGroups = paths.groupBy { Files.isDirectory(it) }
        def dirs = pathGroups[true] ?: []
        def files = pathGroups[false] ?: []

        /* grouped files */
        files.groupBy { it.getParent() }.each { dir, fileGroup ->
            def groupIterator = fileGroup.iterator()
            while (groupIterator) {
                def objs = groupIterator.take(Globals.MAX_BULK_LOAD).collect {
                    new Ds3Object(it.getFileName().toString(), Files.size(it))
                }
                def job = helper.startWriteJob(name, helper.addPrefixToDs3ObjectsList(objs, remoteDir))
                job.transfer(new PrefixAdderObjectChannelBuilder(new FileObjectPutter(dir), remoteDir))
            }
        }

        /* directories */
        dirs.each { dir ->
            def objIterator = helper.listObjectsForDirectory(dir).iterator()
            while (objIterator) {
                def objs = objIterator.take(Globals.MAX_BULK_LOAD).collect()
                def job = helper.startWriteJob(name, helper.addPrefixToDs3ObjectsList(objs, remoteDir))
                job.transfer(new PrefixAdderObjectChannelBuilder(new FileObjectPutter(dir), remoteDir))
            }
        }

        return reload()
    }

    BpBucket putBulk(String pathStr, String remoteDir = '') {
        return putBulk([pathStr], remoteDir)
    }

    /**
     * Writes objects to specified directory
     * @param objectNames names of the objects to get
     * @param destinationPathStr directory to write to
     */
    void getBulk(List<String> objectNames, String destinationPathStr = '') {

        /* Make sure download directory isn't a file, create directory if it doesn't exist */
        def destinationPath = Paths.get(destinationPathStr)
        if (Files.isRegularFile(destinationPath)) {
            throw new BpException("$destinationPathStr is not a directory!")
        } else if (!Files.exists(destinationPath)) {
            Files.createDirectory(destinationPath)
        }

        /* Read objects, Globals.MAX_BULK_LOAD at a time */
        def nameIterator = objectNames.iterator()
        while (nameIterator) {
            def names = nameIterator.take(Globals.MAX_BULK_LOAD).collect()
            def dS3Objects = objects(*names).collect { new Ds3Object(it.getName()) }

            def readJob = this.helper.startReadJob(this.name, dS3Objects)
            readJob.transfer(new FileObjectGetter(destinationPath))
        }
    }

    BpBucket getBulk(String objectName, String pathStr = '') {
        return getBulk([objectName], pathStr)
    }

    /**
     * @param objectNames objects names to return
     * @return list all objects in bucket or objects with given names
     */
    List<BpObject> objects(String... objectNames) {
        def wantedObjects = listBucketResult.objects
        if (objectNames.length != 0) {
            wantedObjects = listBucketResult.objects.findAll { objectNames.contains(it.getKey()) }
        }

        return contentsToBpObjects(wantedObjects)
    }

    List<BpObject> objects(List<String> objectNames) {
        return objects(*objectNames)
    }

    /**
     * @param objectName the name of the object to return
     * @return BpObject with given name
     */
    BpObject object(String objectName) {
        def objs = objects(objectName)
        if (objs.size() == 1) {
            return objs[0]
        } else {
            throw new BpException("${objs.size()} objects named '$objectName' found!")
        }
    }

    Integer size() {
        return this.objects().size()
    }

    String toString() {
        return "name: $name, client: {$client}"
    }

    /** Converts an array of Contents objects to ds3Objects */
    private contentsToBpObjects(contents) {
        return contents.collect { new BpObject(it, this, this.client) }
    }

}
