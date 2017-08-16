import com.spectralogic.dsl.models.BpClientFactory
import org.junit.Test
import java.util.Random

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNull

/** Tests BpClient */
public class BpClientTest {

  @Test
  public void testClient() throws IOException {
    def client = new BpClientFactory().create()
    def bucketName = 'test_bucket_' + (new Random().nextInt(10 ** 4))
    def bucket = client.createBucket(bucketName)
    assertEquals bucketName, bucket.name
    assertEquals bucket.name, client.bucket(bucketName).name
    bucket.delete()
    assertNull client.bucket(bucketName)
  }

}
