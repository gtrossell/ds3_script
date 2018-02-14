package com.spectralogic.dsl.test.models

import com.spectralogic.dsl.models.BpClientBuilder
import org.junit.Test

/** Tests BpClient */
class BpClientTest extends GroovyTestCase {

    @Test
    void testClient() throws IOException {
        def client = new BpClientBuilder().create()

        def bucketCount = client.buckets().size()

        def bucketName = 'test_bucket_' + (new Random().nextInt(10**4))
        def bucket = client.createBucket(bucketName)

        assertEquals bucketCount + 1, client.buckets().size()
        assertEquals bucketName, bucket.name
        assertEquals bucket.name, client.bucket(bucketName).name
        bucket.delete()
        shouldFail { client.bucket(bucketName) }
    }

}
