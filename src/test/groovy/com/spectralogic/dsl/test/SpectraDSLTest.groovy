package com.spectralogic.dsl.test

import com.spectralogic.dsl.helpers.Environment
import com.spectralogic.dsl.models.BpClientBuilder
import org.junit.Test

/**
 * Tests everything that the SpectraDSL class uses 
 * TODO: Test the actual SpectraDSL class
 */
class SpectraDSLTest extends GroovyTestCase {

    @Test
    void testCreateBpClient() throws IOException {
        def env = new Environment()
        def invalidVars = [null, '']
        assert !(env.getEndpoint() in invalidVars)
        assert !(env.getAccessKey() in invalidVars)
        assert !(env.getSecretKey() in invalidVars)
        def client = new BpClientBuilder().create()

        /* create a bucket to test if connection is good */
        def bucketName = 'test_bucket_' + (new Random().nextInt(10**4))
        def bucket = client.createBucket(bucketName)
        assertEquals bucketName, bucket.name
        bucket.delete()
        shouldFail { client.bucket(bucketName) }
    }

}
