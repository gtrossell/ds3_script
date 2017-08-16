import com.spectralogic.dsl.helpers.Environment
import com.spectralogic.dsl.models.BpClientFactory
import org.junit.Test
import java.util.Random

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNull

/**
 * Tests everything that the SpectraDSL class uses 
 * TODO: Test the actual SpectraDSL class
 */
public class SpectraDSLTest {
  @Test
  public void testCreateBpClient() throws IOException {
    def env = new Environment()
    def invalidVars = [null, '']
    assert !(env.getEndpoint() in invalidVars)
    assert !(env.getAccessKey() in invalidVars)
    assert !(env.getSecretKey() in invalidVars)
    def client = new BpClientFactory().create()
    /* have to create a bucket to test if the connect s good */
    def bucketName = 'test_bucket_' + (new Random().nextInt(10 ** 4))
    def bucket = client.createBucket(bucketName)
    assertEquals bucketName, bucket.name
    bucket.delete()
    assertNull client.bucket(bucketName)
  }
}
