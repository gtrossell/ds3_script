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
    def homePath = new File("").getAbsoluteFile().toString()
    def tmpPath = "$homePath/test-data/tmp/"
    FileUtils.cleanDirectory(new File(tmpPath))

    def client = new BpClientBuilder().create()
    def bucketName = 'test_bucket_' + (new Random().nextInt(10 ** 4))
    def bucket = client.createBucket(bucketName)

    /* test put */
    bucket.putBulk("${homePath}/test-data/dir2")
    bucket.putBulk(["${homePath}/test-data/dir1/txt1.txt",
                    "${homePath}/test-data/dir1/txt2.txt",
                    "${homePath}/test-data/dir1/txt3.txt"])
    bucket.putBulk("$homePath/test-data/dir3/txt6.txt", "remote/")
    bucket.putBulk("$homePath/test-data/dir4/", "remote2/")
    def fileCount = 8

    /* test bucket metadata */
    assertEquals fileCount, bucket.objects().size()
    assertEquals bucket.objects().size(), bucket.reload().objects().size()
    assertEquals 'txt1.txt', bucket.object('txt1.txt').name
    assertEquals 2, bucket.objects('txt2.txt', 'txt3.txt').size()
    assert 10000 < bucket.object('txt2.txt').size

    /* test get specific objects */
    def objNames = ['txt1.txt','txt2.txt','txt3.txt']
    shouldFail { bucket.getBulk(['doesnt_exist.txt'], tmpPath) }
    bucket.getBulk(objNames, tmpPath)
    objNames.each {
      def file = Paths.get(tmpPath)
      assert Files.exists(file)
    }

    /* test get objects in remote directory */
    bucket.getBulk("remote/txt6.txt", tmpPath)
    def file = new File("${tmpPath}remote/txt6.txt")
    assert file.exists()

    /* test get object of remote directory */
    bucket.getBulk("remote2/", tmpPath)
    [new File("${tmpPath}remote2/txt7.txt"), new File("${tmpPath}remote2/txt8.txt")].each {
      assert it.exists()
    }

    FileUtils.cleanDirectory(new File(tmpPath))

    /* test object deletion */
    shouldFail BpException.class, { bucket.delete() }
    bucket.deleteObjects([bucket.object('txt1.txt')])
    assertEquals fileCount - 1, bucket.objects().size()
    bucket.deleteAllObjects()
    assertEquals 0, bucket.objects().size()

    /* test bucket deletion */
    bucket.delete()
    shouldFail { client.bucket(bucketName) }
  }

}
