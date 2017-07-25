package spectra

import com.spectralogic.ds3client.models.ListBucketResult
import com.spectralogic.ds3client.models.ChecksumType
import com.spectralogic.ds3client.commands.interfaces.AbstractResponse
import com.spectralogic.ds3client.commands.GetBucketResponse
import com.spectralogic.ds3client.Ds3ClientImpl

/** Represents a BlackPearl bucket, extended from GetBucketResponse */
class BpBucket extends GetBucketResponse {
  Ds3ClientImpl client
  String name

  def BpBucket(GetBucketResponse response, Ds3ClientImpl client) {
    super(response.getListBucketResult(), 
          response.getChecksum(), 
          response.getChecksumType())
    this.client = client
    this.name = response.getListBucketResult().getName()
  }

  /** 
   * @param objectNames objects names to return
   * @return list all objects in bucket or objects with given names 
   */
  def objects(String ...objectNames) {
    def allObjects = this.getListBucketResult().getObjects()
    def result = []
    if (objectNames.length == 0) {
      result = contentsToBpObjects(allObjects)
    } else {
      def wantedObjects = []
      this.getListBucketResult().getObjects().each { contents -> 
        if (objectNames.contains(contents.getKey())) wantedObjects << contents
      }
      result = contentsToBpObjects(wantedObjects)
    }
    return result
  }

  /** 
   * @param objectName the name of the object to return
   * @return BpObject with given name 
   */
  def object(String objectName) {
    def allObjects = this.getListBucketResult().getObjects()
    def result = null
    this.getListBucketResult().getObjects().each { contents ->
      if (contents.getKey() == objectName) {
        result = contentsToBpObjects(contents)[0]
      }
    }
    return result
  }

  /** Converts an array of Contents objects to ds3Objects */
  private contentsToBpObjects(contents) {
    def bpObjects = []
    contents.each { bpObjects << new BpObject(it, this, this.client) }
    return bpObjects
  }
}
