package spectra

import com.spectralogic.ds3client.Ds3ClientImpl
import com.spectralogic.ds3client.commands.GetBucketResponse
import com.spectralogic.ds3client.commands.GetBucketRequest
import com.spectralogic.ds3client.commands.PutBucketRequest

class BpClient extends Ds3ClientImpl {
  
  def BpClient(Ds3ClientImpl ds3Client) {
    super(ds3Client.getNetClient())
  }

  /** @return BpBucket of bucket with given name */
  def bucket(String bucketName) {
    // TODO: if no bucket is found return null and issue warning
    def response = this.getBucket(new GetBucketRequest(bucketName))
    return new BpBucket(response, this)
  }

  /** @return BpBucket of newly created BP bucket */
  def createBucket(String name, String dataPolicyId="") {
    // TODO: check if bucket already exists
    // TODO: impliment dataPolicyId
    this.putBucket(new PutBucketRequest(name))
    bucket(name)
  }

}
