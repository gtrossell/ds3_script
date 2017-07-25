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
    Ds3ClientImpl.metaClass.bucket { String bucketName ->
      def response = delegate.getBucket(new GetBucketRequest(bucketName))
      def bucket = new BpBucket(response, delegate)
      // bucket.name = bucketName
      // bucket.client = delegate
      // return bucket
      return bucket
    }

    /** Converts an array of Contents objects to ds3Objects */
    // GetBucketResponse.metaClass.contentsToBpObjects { contents ->
    //   def bpObjects = []
    //   def bucket = delegate
    //   contents.each { bpObjects << new BpObject(it, bucket, delegate.client) }
    //   return bpObjects
    // }
    /** 
     * returns list of objects in bucket if no arguments are given
     * returns a single object if one name is given
     * returns a list of all objects listed
     */
    // GetBucketResponse.metaClass.objects { String ...objectNames ->
    //   def allObjects = delegate.getListBucketResult().getObjects()
    //   def result
    //   if (objectNames.length == 0) {
    //     result = contentsToBpObjects(allObjects)
    //   } else if (objectNames.length == 1) {
    //     delegate.getListBucketResult().getObjects().each { contents ->
    //       if (contents.getKey() == objectNames[0]) {
    //         result = contentsToBpObjects(contents)[0]
    //       }
    //     }
    //   } else {
    //     def wantedObjects = []
    //     delegate.getListBucketResult().getObjects().each { contents -> 
    //       if (objectNames.contains(contents.getKey())) wantedObjects << contents
    //     }
    //     result = contentsToBpObjects(wantedObjects)
    //   }
    //   return result
    // }
    // /** Bucket name and client */
    // GetBucketResponse.metaClass.name = ""
    // GetBucketResponse.metaClass.client = null
  }

  /** Returns a BP client */
  def createBpClient(String endpoint, String accessid, String secretKey, Boolean https=false) {
    def cred = new Credentials(accessid, secretKey)
    Ds3ClientBuilder.create(endpoint, cred).withHttps(https).build()
  }

  /** Creates file path from string  */
  def Path dirPath(String dirName) {
    Paths.get(dirName)
  }

}
