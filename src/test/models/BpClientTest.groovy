package spectra.test.models

import java.util.Random

import spectra.models.BpClient
import spectra.test.helpers.DummyShell

/** Tests BpClient class. Enviroment variables for client must be set */
class BpClientTest extends GroovyTestCase {

  void testCreateBucket() {
    def client = new DummyShell().createBpClient()
    def bucketName = 'test_bucket_' + (new Random().nextInt(10 ** 4))
    def bucket = client.createBucket(bucketName)
    assertEquals bucketName, bucket.name
    assertEquals bucket.name, client.bucket(bucketName).name
    bucket.delete()
    assertNull client.bucket(bucketName)
  }

}
