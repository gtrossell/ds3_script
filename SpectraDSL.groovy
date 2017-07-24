package spectra

@GrabResolver(name='Spectra-Github', root='http://dl.bintray.com/spectralogic/ds3/')
@Grapes([
  @Grab(group='com.spectralogic.ds3', module='ds3-sdk', version='3.4.0'),
  @Grab(group='org.slf4j', module='slf4j-simple', version='1.7.21')
])

import com.spectralogic.ds3client.models.Bucket
import com.spectralogic.ds3client.Ds3Client
import com.spectralogic.ds3client.Ds3ClientImpl
import com.spectralogic.ds3client.models.common.Credentials
import com.spectralogic.ds3client.Ds3ClientBuilder
import com.spectralogic.ds3client.models.ListBucketResult
import com.spectralogic.ds3client.commands.GetBucketRequest
import com.spectralogic.ds3client.models.common.Credentials

import groovy.transform.*

import static spectra.ClientCommands.*
import spectra.*

/**
 * This is the customized SpectraShell that contains the DSL and acts similar
 * to GroovyShell
 */
abstract class SpectraDSL extends Script {

  def SpectraDSL() {
    /** gets bucket */
    Ds3ClientImpl.metaClass.bucket { String bucketName ->
      delegate.getBucket(new GetBucketRequest(bucketName))
    }
  }

  /** Returns a BP client */
  def createBpClient(String endpoint, String accessid, String secretKey, Boolean https=false) {
    def cred = new Credentials(accessid, secretKey)
    Ds3ClientBuilder.create(endpoint, cred).withHttps(https).build()
  }

}
