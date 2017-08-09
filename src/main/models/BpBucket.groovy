package spectra.models

import com.spectralogic.ds3client.helpers.channelbuilders.PrefixAdderObjectChannelBuilder
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers
import com.spectralogic.ds3client.helpers.FileObjectPutter
import com.spectralogic.ds3client.models.ListBucketResult
import com.spectralogic.ds3client.models.ChecksumType
import com.spectralogic.ds3client.models.bulk.Ds3Object
import com.spectralogic.ds3client.commands.interfaces.AbstractResponse
import com.spectralogic.ds3client.commands.GetBucketResponse
import com.spectralogic.ds3client.commands.DeleteBucketRequest
import com.spectralogic.ds3client.Ds3ClientImpl

import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files

/** Represents a BlackPearl bucket, extended from GetBucketResponse */
class BpBucket extends GetBucketResponse {
  Ds3ClientImpl client
  String name
  ListBucketResult listBucketResult

  def BpBucket(GetBucketResponse response, Ds3ClientImpl client) {
    super(response.getListBucketResult(), 
          response.getChecksum(), 
          response.getChecksumType())
    this.listBucketResult = response.getListBucketResult()
    this.client = client
    this.name = response.getListBucketResult().getName()
  }

  /** @return the current version of this bucket. Doesn't change this object */
  def reload() {
    this.listBucketResult = client.bucket(this.name).getListBucketResult()
    this
  }

  /** 
   * Deletes bucket, but only if the bucket is already empty so that data is not
   * accidentally deleted
   * @return true if bucket was deleted
   */
  def delete() {
    // TODO: figure out if there are still objects or not
    client.deleteBucket(new DeleteBucketRequest(this.name))
  }

  /** Deletes all objects inside it */
  def empty() {
    deleteObjects(*objects())
  }

  /** Deletes each of the objects in the given list */
  def deleteObjects(BpObject ...objects) {
    objects.each { it.delete() }
    reload()
  }

  /**
   * @param paths Directories and files to upload
   * @param remoteDir Directory on BP to use as a root for uploading (ie 'Dir1/')
   * Puts each file and file in each directory given into the bucket
   */
  def putBulk(List<String> pathStrs, String remoteDir='') {
    def paths = []
    pathStrs.each { pathStr ->
      paths << Paths.get(pathStr)
    }
    if (remoteDir && remoteDir[-1] != '/') remoteDir += '/'

    // TODO: add true logging
    /* Group files into common directories */
    def helper = Ds3ClientHelpers.wrap(this.client)
    def objects = [:] /* key: directory val: array of Ds3Objects */
    paths.each { path ->
      if (!Files.exists(path)) {
        println "WARNING: '$path' does not exist, skipping"
      } else if (Files.isDirectory(path)) {
        def pathStr = path.toString()
        if (objects[pathStr] == null) objects[pathStr] = []
        objects[pathStr] << helper.listObjectsForDirectory(path)
      } else if (Files.isRegularFile(path)) {
        def pathStr = path.getParent().toString()
        if (objects[pathStr] == null) objects[pathStr] = []
        objects[pathStr] << new Ds3Object((String) path.getFileName(), Files.size(path))
      } else {
        println "WARNING: '$path' is not a directory or regular file, skipping"
      }
    }

    /* Put job that maintains subdirectory order and remoteDir prefix */
    objects.each { dir, objs ->
      objs = objs.flatten() /* might be lists in list */
      objs = helper.addPrefixToDs3ObjectsList(objs, remoteDir)
      def job = helper.startWriteJob(this.name, objs)
      job.transfer(new PrefixAdderObjectChannelBuilder(
        new FileObjectPutter(Paths.get(dir)), remoteDir))
    }

    reload()
  }

  /**
   * Same as putBulk with one file, but allows user to name the name the object 
   * that is created. Also, it returns the created object
   * @param objectName  name for the new object
   * @param filePath    Location of the file to upload
   * @return  BpObject of the newly uploaded object
   */
  def putObject(String objectName, Path path) {
    // TODO: objectName can also include a path to be made in the bucket
    // TODO: add true logging
    // if (!Files.exists(path)) {
    //   println "ERROR: File does not exist"
    //   return null
    // } else if (Files.isDirectory(path)) {
    //   println "ERROR: Path given is a directory"
    //   return null
    // }

    // def job = helper.startWriteJob(this.name, new Ds3Object((String) path.getFileName(), Files.size(path)))
    // job.transfer(new FileObjectPutter(path.getParent()))
  }

  /** 
   * @param objectNames objects names to return
   * @return list all objects in bucket or objects with given names 
   */
  def objects(String ...objectNames) {
    // TODO: if object cannot be found, reload the bucket
    def wantedObjects = []
    if (objectNames.length == 0) {
      wantedObjects = this.listBucketResult.objects
    } else {
      this.listBucketResult.objects.each { contents -> 
        if (objectNames.contains(contents.getKey())) wantedObjects << contents
      }
    }
    return contentsToBpObjects(wantedObjects)
  }

  /** 
   * @param objectName the name of the object to return
   * @return BpObject with given name 
   */
  def object(String objectName) {
    def objs = objects(objectName)
    return (objs.size() == 1 ? objs[0] : null)
  }

  /** Converts an array of Contents objects to ds3Objects */
  private contentsToBpObjects(contents) {
    def bpObjects = []
    contents.each { bpObjects << new BpObject(it, this, this.client) }
    return bpObjects
  }
}
