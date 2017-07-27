package spectra

@GrabResolver(name='Spectra-Github', root='http://dl.bintray.com/spectralogic/ds3/')
@Grapes([
  @Grab(group='com.spectralogic.ds3', module='ds3-sdk', version='3.4.0'),
  @Grab(group='org.slf4j', module='slf4j-simple', version='1.7.21')
])

import com.spectralogic.ds3client.commands.*
import com.spectralogic.ds3client.Ds3Client
import com.spectralogic.ds3client.Ds3ClientBuilder
import com.spectralogic.ds3client.Ds3ClientImpl
import com.spectralogic.ds3client.models.Bucket
import com.spectralogic.ds3client.models.bulk.Ds3Object
import com.spectralogic.ds3client.models.common.Credentials
import com.spectralogic.ds3client.models.Contents
import com.spectralogic.ds3client.models.ListBucketResult
import com.spectralogic.ds3client.models.User

import groovy.transform.*

import java.nio.file.Path
import java.nio.file.Paths

import spectra.*

/**
 * This is the customized SpectraShell that contains the DSL and acts similar
 * to GroovyShell
 */
abstract class SpectraDSL extends Script {

  def SpectraDSL() {
    /** gets bucket */
    // Ds3ClientImpl.metaClass.bucket { String bucketName ->
    //   def response = delegate.getBucket(new GetBucketRequest(bucketName))
    //   return new BpBucket(response, delegate)
    // }

  }

  /** Returns a BP client */
  def createBpClient(String endpoint, String accessid, String secretKey, Boolean https=false) {
    // TODO: allow no arguments and use the enviroment variables
    def cred = new Credentials(accessid, secretKey)
    new BpClient(Ds3ClientBuilder.create(endpoint, cred).withHttps(https).build())
  }

  /** Creates directory or file path from string  */
  def Path filePath(String dirName) {
    Paths.get(dirName)
  }

}
