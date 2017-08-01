package spectra.test.models

import java.util.Random

import spectra.models.BpClient
import spectra.test.helpers.DummyShell

import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files

/** Tests BpClient class. Enviroment variables for client must be set */
class BpObjectTest extends GroovyTestCase {

  void testAll() {
    def shell = new DummyShell()
    def client = shell.createBpClient()
    def bucketName = 'test_bucket_' + (new Random().nextInt(10 ** 4))
    def bucket = client.createBucket(bucketName)
    
    def homePath = new File("").getAbsoluteFile().toString()
    def fileName = 'txt1.txt'
    bucket.putBulk("${homePath}/test-data/dir1/" + fileName)
    def object = bucket.object(fileName)
    def meta = object.getMetadata()
    assert meta['name']
    assert meta['size'] == object.size()
    assert 10 < object.size()
    assert meta['owner']
    assert meta['bucketName']

    // write
    def dirPath = "${homePath}/test-data/tmp/"
    object.writeTo(shell.filePath(dirPath))
    def file = shell.filePath(dirPath + object.name)
    assert Files.exists(file)
    Files.delete(file)

    // delete
    assert object.delete()
    assertNull bucket.object(fileName)
    bucket.delete()
  }

}
