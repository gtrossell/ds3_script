package spectra

import com.spectralogic.ds3client.models.ListBucketResult
import com.spectralogic.ds3client.models.ChecksumType
import com.spectralogic.ds3client.commands.interfaces.AbstractResponse
import com.spectralogic.ds3client.commands.GetBucketResponse
import com.spectralogic.ds3client.Ds3ClientImpl

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
   * returns list of objects in bucket if no arguments are given
   * returns a single object if one name is given
   * returns a list of all objects listed
   */
  def objects(String ...objectNames) {
    def allObjects = this.getListBucketResult().getObjects()
    def result
    if (objectNames.length == 0) {
      result = contentsToBpObjects(allObjects)
    } else if (objectNames.length == 1) {
      this.getListBucketResult().getObjects().each { contents ->
        if (contents.getKey() == objectNames[0]) {
          result = contentsToBpObjects(contents)[0]
        }
      }
    } else {
      def wantedObjects = []
      this.getListBucketResult().getObjects().each { contents -> 
        if (objectNames.contains(contents.getKey())) wantedObjects << contents
      }
      result = contentsToBpObjects(wantedObjects)
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
