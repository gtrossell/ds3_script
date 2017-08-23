package com.spectralogic.dsl.models

import com.spectralogic.dsl.exceptions.BpException
import com.spectralogic.ds3client.helpers.channelbuilders.PrefixAdderObjectChannelBuilder
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers
import com.spectralogic.ds3client.helpers.FileObjectPutter
import com.spectralogic.ds3client.models.ChecksumType
import com.spectralogic.ds3client.models.Contents
import com.spectralogic.ds3client.models.ListBucketResult
import com.spectralogic.ds3client.models.bulk.Ds3Object
import com.spectralogic.ds3client.commands.DeleteBucketRequest
import com.spectralogic.ds3client.commands.DeleteObjectsRequest
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
  private final helper
  private ListBucketResult listBucketResult
  final String name

  BpBucket(GetBucketResponse response, Ds3ClientImpl client) {
    super(response.getListBucketResult(), 
          response.getChecksum(), 
          response.getChecksumType())
    this.client = client
    this.helper = Ds3ClientHelpers.wrap(client)
    this.listBucketResult = response.getListBucketResult()
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
  void delete() {
    if (this.objects()) {
      throw new BpException("Bucket must be empty to delete!")
    }
    client.deleteBucket(new DeleteBucketRequest(this.name))
  }

  /** Deletes all objects inside it */
  BpBucket empty() {
    return deleteObjects(*contentsToBpObjects(helper.listObjects(name)))
  }

  /** Recursively delete objects in max 1000 object bulks */
  BpBucket deleteObjects(BpObject ...objects) {
    if (objects.size() < 1) return this
    
    def objIterator = objects.iterator()
    while (objIterator) {
      def currentBatch = objIterator.take(1_000)
      client.deleteObjects(new DeleteObjectsRequest(name, currentBatch.collect { it.name }))
    }

    return reload()
  }

  /**
   * @param paths Directories and files to upload
   * @param remoteDir Directory on BP to use as a root for uploading (ie 'Dir1/')
   * Puts each file and file in each directory given into the bucket
   */
  BpBucket putBulk(List<String> pathStrs, String remoteDir='') {
    if (remoteDir && remoteDir[-1] != '/') remoteDir += '/'

    /* Group files into common directories */
    def objects = [:] /* key: directory val: array of Ds3Objects */
    def addObjects = { pathStr, ds3Objects ->
      if (objects[pathStr] == null) objects[pathStr] = []
      objects[pathStr] = (objects[pathStr] << ds3Objects).flatten()
    }

    pathStrs.each {
      def path = Paths.get(it)
      if (!Files.exists(path)) {
        throw BpException("'$path' does not exist!")
      } else if (Files.isDirectory(path)) {
        addObjects(path.toString(), helper.listObjectsForDirectory(path))
      } else if (Files.isRegularFile(path)) {
        addObjects(path.getParent().toString(), 
          new Ds3Object(path.getFileName().toString(), Files.size(path)))
      } else {
        throw BpException("'$path' is not a directory or regular file")
      }
    }

    /* Put job that maintains subdirectory order and remoteDir prefix */
    objects.each { dir, objs ->
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
