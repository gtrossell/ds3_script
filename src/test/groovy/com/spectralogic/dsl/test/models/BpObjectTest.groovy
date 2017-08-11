import org.junit.Test
import static org.junit.Assert.assertNull

import java.nio.file.Files
import java.nio.file.Paths
import java.util.Random

import com.spectralogic.dsl.helpers.Globals

/** Tests BpObject */
public class BpObjectTest {
  @Test
  public void testObject() throws IOException {
    def client = Globals.createBpClient()
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
    object.writeTo(Paths.get(dirPath))
    def file = Paths.get(dirPath + object.name)
    assert Files.exists(file)
    Files.delete(file)

    // delete
    assert object.delete()
    assertNull bucket.object(fileName)
    bucket.delete()
  }
}