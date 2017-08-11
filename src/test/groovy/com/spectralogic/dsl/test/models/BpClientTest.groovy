import org.junit.Test
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNull

import java.util.Random

import com.spectralogic.dsl.helpers.Globals

/** Tests BpClient */
public class BpClientTest {
  @Test
  public void testClient() throws IOException {
    def client = Globals.createBpClient()
    def bucketName = 'test_bucket_' + (new Random().nextInt(10 ** 4))
    def bucket = client.createBucket(bucketName)
    assertEquals bucketName, bucket.name
    assertEquals bucket.name, client.bucket(bucketName).name
    bucket.delete()
    assertNull client.bucket(bucketName)
  }
}
