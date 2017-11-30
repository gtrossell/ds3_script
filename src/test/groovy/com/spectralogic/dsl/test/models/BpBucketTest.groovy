package com.spectralogic.dsl.test.models

import com.spectralogic.dsl.exceptions.BpException
import com.spectralogic.dsl.models.BpClientBuilder
import org.apache.commons.io.FileUtils
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Paths

/** Tests BpBucket */
class BpBucketTest extends GroovyTestCase {

  @Test
  void testBucket() throws IOException {
    def client = new BpClientBuilder().create()
    def bucketName = 'test_bucket_' + (new Random().nextInt(10 ** 4))
    def bucket = client.createBucket(bucketName)

    // put test objects
    def homePath = new File("").getAbsoluteFile().toString()
    bucket.putBulk("${homePath}/test-data/dir2")
    bucket.putBulk(["${homePath}/test-data/dir1/txt1.txt",
                    "${homePath}/test-data/dir1/txt2.txt",
                    "${homePath}/test-data/dir1/txt3.txt"])
    bucket.putBulk("$homePath/test-data/dir3/txt6.txt", "remote/")

    assertEquals 6, bucket.objects().size()
    assertEquals bucket.objects().size(), bucket.reload().objects().size()
    assertEquals 'txt1.txt', bucket.object('txt1.txt').name
    assertEquals 2, bucket.objects('txt2.txt', 'txt3.txt').size()
    assert 10000 < bucket.object('txt2.txt').size

    def objNames = ['txt1.txt','txt2.txt','txt3.txt']
    shouldFail { bucket.getBulk(['doesnt_exist.txt'], "${homePath}/test-data/tmp/") }
    bucket.getBulk(objNames, "${homePath}/test-data/tmp/")
    objNames.each {
      def file = Paths.get("${homePath}/test-data/tmp/$it")
      assert Files.exists(file)
    }

    bucket.getBulk("remote/txt6.txt", "${homePath}/test-data/tmp/")
    def file = new File("${homePath}/test-data/tmp/remote/txt6.txt")
    assert file.exists()

    FileUtils.cleanDirectory(new File("${homePath}/test-data/tmp/"))

    shouldFail BpException.class, { bucket.delete() }

    bucket.deleteObjects([bucket.object('txt1.txt')])
    assertEquals 5, bucket.objects().size()

    bucket.deleteAllObjects()
    assertEquals 0, bucket.objects().size()
    bucket.delete()
    shouldFail { client.bucket(bucketName) }
  }

}
