package spectra

import com.spectralogic.ds3client.Ds3ClientImpl
import com.spectralogic.ds3client.commands.GetBucketResponse
import com.spectralogic.ds3client.commands.GetBucketRequest

class BpClient extends Ds3ClientImpl {
  
  def BpClient(Ds3ClientImpl ds3Client) {
    super(ds3Client.getNetClient())
  }

  def bucket(String bucketName) {
    def response = this.getBucket(new GetBucketRequest(bucketName))
    return new BpBucket(response, this)
  }

  def createBucket(String name, String dataPolicyId) {
    
  }

}
