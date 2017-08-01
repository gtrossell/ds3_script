package spectra.models

import com.spectralogic.ds3client.commands.DeleteObjectRequest
import com.spectralogic.ds3client.commands.DeleteObjectResponse
import com.spectralogic.ds3client.commands.GetBucketResponse
import com.spectralogic.ds3client.commands.GetObjectRequest
import com.spectralogic.ds3client.commands.spectrads3.GetBulkJobSpectraS3Request
import com.spectralogic.ds3client.Ds3ClientImpl
import com.spectralogic.ds3client.models.bulk.Ds3Object
import com.spectralogic.ds3client.models.Contents
import com.spectralogic.ds3client.models.User

import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

/** Represents a BlackPearl Object, extends Ds3Object */
class BpObject extends Ds3Object {
  BpBucket bucket
  Ds3ClientImpl client
  User owner
  def metadata

  def BpObject(Contents contents, BpBucket bucket, Ds3ClientImpl client) {
    this.name = contents.getKey()
    this.size = contents.getSize()
    this.owner = contents.getOwner()
    this.bucket = bucket
    this.client = client
  }

  /** 
   * Deletes the object from the BP 
   * @return true if object was deleted 
   */
  def delete() {
    try {
      def deleteRequest = new DeleteObjectRequest(bucket.name, this.name)
      def deleteResponse = client.deleteObject(deleteRequest)
      bucket.reload()
    } catch (Exception e) {
      e.printStackTrace()
      return false
    }
    return true
  }

  /** 
   * Write object to given directory.
   * It will create the directory path if it does not exist
   * @param path  directory to write object to
   * @return true if the write was successful
   */
  def writeTo(Path path) {
    // TODO: write to a file or directory
    // TODO: allow use of a string
    try {
      if (!Files.exists(path)) Files.createDirectory(path)
      
      /* convert BpObject to Ds3Object */
      def objectList = [new Ds3Object(this.getName())]
      def bulkRequest = new GetBulkJobSpectraS3Request(bucket.name, objectList)
      def bulkResponse = this.client.getBulkJobSpectraS3(bulkRequest)
      
      def list = bulkResponse.getMasterObjectList()
      for (objects in list.getObjects()) {
        for (obj in objects.getObjects()) {
          def channel = FileChannel.open(
            path.resolve(obj.getName()),
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE
          )
          
          channel.position(obj.getOffset());
          
          client.getObject(new GetObjectRequest(
            bucket.name,
            obj.getName(),
            channel,
            list.getJobId().toString(),
            obj.getOffset()
          ))
        }
      }
    } catch (Exception e) {
      e.printStackTrace()
      return false
    }
    return true
  }

  /** @return size of object in bytes */
  def size() { this.size }

  /** @return map of the metadata */
  def getMetadata() {
    [
      name:       this.name, 
      size:       this.size, 
      owner:      this.owner,
      bucketName: this.bucket.name
    ]
  }
}