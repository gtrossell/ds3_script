import org.junit.Test
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNull

import java.util.Random

import com.spectralogic.dsl.helpers.Globals

/** Tests BpBucket */
public class BpBucketTest {
  @Test
  public void testBucket() throws IOException {
    def client = Globals.createBpClient()
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
    assert 10000 < bucket.object('txt2.txt').size()

    assertFalse bucket.delete()
    bucket.empty()
    assertEquals 0, bucket.objects().size()
    bucket.delete()
    assertNull client.bucket(bucketName)
  }
}
