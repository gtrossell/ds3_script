package spectra

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

class BpObject extends Ds3Object {
  GetBucketResponse bucket
  Ds3ClientImpl client
  User owner
  def metadata

  def BpObject(Contents contents, GetBucketResponse bucket, Ds3ClientImpl client) {
    this.name = contents.getKey()
    this.size = contents.getSize()
    this.owner = contents.getOwner()
    this.bucket = bucket
    this.client = client
  }

  /** Deletes the object from the BP */
  def delete() {
    try {
      def deleteRequest = new DeleteObjectRequest(bucket.name, this.name)
      def deleteResponse = client.deleteObject(deleteRequest)
    } catch (Exception e) {
      e.printStackTrace()
      return false
    }
    return true
  }

  /** Write to given directory */
  def writeTo(path) {
    try {
      if (!Files.exists(path)) {
        Files.createDirectory(path)
      }
      
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

  /** return map of the metadata */
  def getMetadata() {
     metadata = [
                  name:       this.name, 
                  size:       this.size, 
                  owner:      this.owner,
                  bucketName: this.bucket.name
                ]
  }
}
