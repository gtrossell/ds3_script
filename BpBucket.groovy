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
   * Creates object in the bucket using the given file
   * @param objectName  name for the new object
   * @param filePath    Location of the file to upload
   * @return  BpObject of the newly uploaded object
   */
  def createObject(String objectName) {

  }

  /** 
   * @param objectNames objects names to return
   * @return list all objects in bucket or objects with given names 
   */
  def objects(String ...objectNames) {
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
