package com.spectralogic.dsl.models

import com.spectralogic.ds3client.helpers.channelbuilders.PrefixAdderObjectChannelBuilder
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers
import com.spectralogic.ds3client.helpers.FileObjectPutter
import com.spectralogic.ds3client.models.ChecksumType
import com.spectralogic.ds3client.models.Contents
import com.spectralogic.ds3client.models.ListBucketResult
import com.spectralogic.ds3client.models.bulk.Ds3Object
import com.spectralogic.ds3client.commands.DeleteBucketRequest
import com.spectralogic.ds3client.commands.GetBucketResponse
import com.spectralogic.ds3client.commands.interfaces.AbstractResponse
import com.spectralogic.ds3client.Ds3ClientImpl
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** Represents a BlackPearl bucket, extended from GetBucketResponse */
class BpBucket extends GetBucketResponse {
  private final static logger = LoggerFactory.getLogger(BpBucket.class)
  private final Ds3ClientImpl client
  private ListBucketResult listBucketResult
  final String name

  BpBucket(GetBucketResponse response, Ds3ClientImpl client) {
    super(response.getListBucketResult(), 
          response.getChecksum(), 
          response.getChecksumType())
    this.listBucketResult = response.getListBucketResult()
    this.client = client
    this.name = response.getListBucketResult().getName()
  }

  /** @return the current version of this bucket. Doesn't change this object */
  BpBucket reload() {
    this.listBucketResult = client.bucket(this.name).getListBucketResult()
    return this
  }

  /** 
   * Deletes bucket, but only if the bucket is already empty so that data is not
   * accidentally deleted
   * @return true if bucket was deleted
   */
  Boolean delete() {
    if (this.objects()) {
      logger.error("Bucket must be empty to delete!")
      return false
    }
    client.deleteBucket(new DeleteBucketRequest(this.name))
    return true
  }

  /** Deletes all objects inside it */
  BpBucket empty() {
    return deleteObjects(*objects())
  }

  /** Deletes each of the objects in the given list */
  BpBucket deleteObjects(BpObject ...objects) {
    objects.each { it.delete() } // bulk delete 1000 at a time
    return reload()
  }

  /**
   * @param paths Directories and files to upload
   * @param remoteDir Directory on BP to use as a root for uploading (ie 'Dir1/')
   * Puts each file and file in each directory given into the bucket
   */
  BpBucket putBulk(List<String> pathStrs, String remoteDir='') {
    def paths = []
    pathStrs.each { paths << Paths.get(it) } // one linejj
    if (remoteDir && remoteDir[-1] != '/') remoteDir += '/'

    /* Group files into common directories */
    def helper = Ds3ClientHelpers.wrap(this.client)
    def objects = [:] /* key: directory val: array of Ds3Objects */
    paths.each { path ->
      if (!Files.exists(path)) {
        logger.warn("'{}' does not exist, skipping", path)
      } else if (Files.isDirectory(path)) {
        def pathStr = path.toString()
        if (objects[pathStr] == null) objects[pathStr] = []
        objects[pathStr] << helper.listObjectsForDirectory(path)
      } else if (Files.isRegularFile(path)) {
        def pathStr = path.getParent().toString()
        if (objects[pathStr] == null) objects[pathStr] = []
        objects[pathStr] << new Ds3Object((String) path.getFileName(), Files.size(path)) // cast?
      } else {
        logger.warn("'{}' is not a directory or regular file, skipping", path)
      }
    }

    /* Put job that maintains subdirectory order and remoteDir prefix */
    objects.each { dir, objs ->
      objs = objs.flatten()
      objs = helper.addPrefixToDs3ObjectsList(objs, remoteDir)
      def job = helper.startWriteJob(this.name, objs)
      job.transfer(new PrefixAdderObjectChannelBuilder(
        new FileObjectPutter(Paths.get(dir)), remoteDir))
    }

    return reload()
  }

  BpBucket putBulk(String pathStr, String remoteDir='') {
    return putBulk([pathStr], remoteDir)
  }

  /** 
   * @param objectNames objects names to return
   * @return list all objects in bucket or objects with given names 
   */
  List<BpObject> objects(String ...objectNames) {
    def wantedObjects
    if (objectNames.length == 0) {
      wantedObjects = listBucketResult.objects
    } else {
      wantedObjects = listBucketResult.objects.findAll { objectNames.contains(it.getKey()) }
    }
    return contentsToBpObjects(wantedObjects)
  }

  /** 
   * @param objectName the name of the object to return
   * @return BpObject with given name 
   */
  BpObject object(String objectName) {
    def objs = objects(objectName)
    return (objs.size() == 1 ? objs[0] : null)
  }

  String toString() {
    return "name: $name, client: {$client}"
  }

  /** Converts an array of Contents objects to ds3Objects */
  private contentsToBpObjects(contents) {
    def bpObjects = []
    contents.each { bpObjects << new BpObject(it, this, this.client) }
    return bpObjects
  }

}
