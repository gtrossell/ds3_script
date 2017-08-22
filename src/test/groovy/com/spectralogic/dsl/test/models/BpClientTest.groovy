import com.spectralogic.dsl.models.BpClientBuilder
import org.junit.Test
import java.util.Random

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNull

/** Tests BpClient */
public class BpClientTest extends GroovyTestCase {

  @Test
  public void testClient() throws IOException {
    def client = new BpClientBuilder().create()
    def bucketName = 'test_bucket_' + (new Random().nextInt(10 ** 4))
    def bucket = client.createBucket(bucketName)
    assertEquals bucketName, bucket.name
    assertEquals bucket.name, client.bucket(bucketName).name
    bucket.delete()
    shouldFail { client.bucket(bucketName) }
  }

}
