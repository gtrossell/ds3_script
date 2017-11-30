package com.spectralogic.dsl.models

import com.spectralogic.ds3client.commands.GetObjectRequest
import com.spectralogic.ds3client.commands.spectrads3.GetBulkJobSpectraS3Request
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

import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

/** Represents a BlackPearl bucket, extended from GetBucketResponse */
class BpBucket extends GetBucketResponse {
    private final BpClient client
    private final helper
    private ListBucketResult listBucketResult
    final String name

    BpBucket(GetBucketResponse response, BpClient client) {
        super(response.getListBucketResult(),
                response.getChecksum(),
                response.getChecksumType())
        this.client = client
        this.helper = Ds3ClientHelpers.wrap(client)
        this.listBucketResult = response.getListBucketResult()
        this.name = response.getListBucketResult().getName()
    }

    /** @return the current version of this bucket. Doesn't change this object  */
    BpBucket reload() {
        this.listBucketResult = client.bucket(this.name).getListBucketResult()
        return this
    }

    /**
     * Deletes bucket, but only if the bucket is already empty so that data is not
     * accidentally deleted
     * @return true if bucket was deleted
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
     * @param pathStr directory to write to
     */
    void getBulk(List<String> objectNames, String pathStr = '') {
        def path = Paths.get(pathStr)
        if (Files.isRegularFile(path)) {
            throw new BpException("$pathStr is not a directory!")
        } else if (!Files.exists(path)) {
            Files.createDirectory(path)
        }

        def nameIterator = objectNames.iterator()
        while (nameIterator) {
            def names = nameIterator.take(Globals.MAX_BULK_LOAD).collect()
            def dS3Objects = objects(*names).collect { new Ds3Object(it.getName()) }
            def bulkRequest = new GetBulkJobSpectraS3Request(name, dS3Objects)
            def bulkResponse = this.client.getBulkJobSpectraS3(bulkRequest)

            /* Create any BP remote directories in the given path */
            names.each { String fileLoc ->
                def remoteDir = fileLoc.split('/').dropRight(1).join('/')
                remoteDir = pathStr.endsWith('/') ? "$pathStr$remoteDir" : "$pathStr/$remoteDir"

                def dir = new File(remoteDir)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
            }

            def list = bulkResponse.getMasterObjectList()
            for (objects in list.getObjects()) {
                for (obj in objects.getObjects()) {
                    def channel = FileChannel.open(
                            path.resolve(obj.getName()),
                            StandardOpenOption.WRITE,
                            StandardOpenOption.CREATE
                    )

                    channel.position(obj.getOffset())

                    client.getObject(new GetObjectRequest(
                            name,
                            obj.getName(),
                            channel,
                            list.getJobId().toString(),
                            obj.getOffset()
                    ))
                }
            }
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
        def wantedObjects
        if (objectNames.length == 0) {
            wantedObjects = listBucketResult.objects
        } else {
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

    String toString() {
        return "name: $name, client: {$client}"
    }

    /** Converts an array of Contents objects to ds3Objects */
    private contentsToBpObjects(contents) {
        return contents.collect { new BpObject(it, this, this.client) }
    }

}
