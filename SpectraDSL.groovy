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

  }

  /** Returns a BP client */
  def createBpClient(String endpoint="", String accessId="", 
                      String secretKey="", Boolean https=false) {
    // TODO: add real logging
    def env = System.getenv()
    endpoint = endpoint ?: env['DS3_ENDPOINT']
    accessId = accessId ?: env['DS3_ACCESS_KEY']
    secretKey = secretKey ?: env['DS3_SECRET_KEY']
    if (!endpoint || !accessId || !secretKey) {
      println "[Error] Endpoint, Access ID, and/or Sectret Key is not set!\n" +
              "\tTry setting the enviroment or method variable(s)"
    }
    
    def cred = new Credentials(accessId, secretKey)
    new BpClient(Ds3ClientBuilder.create(endpoint, cred).withHttps(https).build())
  }

  /** Creates directory or file path from string  */
  def Path filePath(String dirName) {
    Paths.get(dirName)
  }

}
