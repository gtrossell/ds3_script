package com.spectralogic.dsl.test.models

import com.spectralogic.dsl.models.BpClientBuilder
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Paths

/** Tests BpObject */
class BpObjectTest extends GroovyTestCase {

    @Test
    void testObject() throws IOException {
        def client = new BpClientBuilder().create()
        def bucketName = 'test_bucket_' + (new Random().nextInt(10**4))
        def bucket = client.createBucket(bucketName)

        def homePath = new File("").getAbsoluteFile().toString()
        def fileName = 'txt1.txt'
        bucket.putBulk("${homePath}/test-data/dir1/" + fileName)
        def object = bucket.getObject(fileName)
        assert object.exists()
        def meta = object.getMetadata()
        // TODO test metadata

        /* write */
        def dirPath = "${homePath}/test-data/tmp/"
        object.writeTo(dirPath)
        def file = Paths.get(dirPath + object.name)
        assert Files.exists(file)
        Files.delete(file)

        /* delete */
        object.delete()
        assert bucket.isEmpty()
        assert !object.exists()
        bucket.delete()
    }

}
