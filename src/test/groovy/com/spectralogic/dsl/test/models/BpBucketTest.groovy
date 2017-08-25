import com.spectralogic.dsl.exceptions.BpException
import com.spectralogic.dsl.models.BpClientBuilder
import org.junit.Test

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
    // TODO: prevent user from having two named the same?
    assertEquals 5, bucket.objects().size()
    assertEquals bucket.objects().size(), bucket.reload().objects().size()
    assertEquals 'txt1.txt', bucket.object('txt1.txt').name
    assertEquals 2, bucket.objects('txt2.txt', 'txt3.txt').size()
    assert 10000 < bucket.object('txt2.txt').size

    shouldFail BpException.class, { bucket.delete() }
    bucket.empty()
    assertEquals 0, bucket.objects().size()
    bucket.delete()
    shouldFail { client.bucket(bucketName) }
  }

}
