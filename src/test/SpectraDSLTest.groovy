package spectra.test

import java.util.Random

import spectra.models.BpClient
import spectra.helpers.Environment
import spectra.test.helpers.DummyShell

/** Tests SpectraDSL class. Enviroment variables for client must be set */
class SpectraDSLTest extends GroovyTestCase {

  void testCreateBpClient() {
    def env = new Environment()
    def invalidVars = [null, '']
    assert !(env.getEndpoint() in invalidVars)
    assert !(env.getAccessKey() in invalidVars)
    assert !(env.getSecretKey() in invalidVars)
    def client = new DummyShell().createBpClient()
    /* have to create a bucket to test if the connect s good */
    def bucketName = 'test_bucket_' + (new Random().nextInt(10 ** 4))
    def bucket = client.createBucket(bucketName)
    assertEquals bucketName, bucket.name
    bucket.delete()
    assertNull client.bucket(bucketName)
  }

}
