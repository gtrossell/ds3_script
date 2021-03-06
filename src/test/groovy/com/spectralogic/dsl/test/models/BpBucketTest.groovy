package com.spectralogic.dsl.test.models

import com.spectralogic.dsl.models.BpClientBuilder
import org.apache.commons.io.FileUtils
import org.junit.Test

class BpBucketTest extends GroovyTestCase {
    def homePath = new File("").getAbsoluteFile().toString()
    def tmpPath = "$homePath/test-data/tmp/"

    def createContext() {
        FileUtils.cleanDirectory(new File(tmpPath))

        def client = new BpClientBuilder().create()
        def bucketName = 'test_bucket_' + (new Random().nextInt(10**4))

        return [client.createBucket(bucketName), bucketName, client]
    }

    @Test
    void testDeleteBucket() {
        def (bucket, bucketName, client) = createContext()
        bucket.delete()
        assertFalse client.getBucket(bucketName).exists
    }

    @Test
    void testDeleteObjects() {
        def (bucket, bucketName, client) = createContext()

        try {
            assertEquals 0, bucket.getSize()
            bucket.putBulk(["${homePath}/test-data/dir1/txt1.txt",
                            "${homePath}/test-data/dir1/txt2.txt",
                            "${homePath}/test-data/dir1/txt3.txt",
                            "${homePath}/test-data/dir1/txt4.txt"])
            assertEquals 4, bucket.getSize()

            bucket.deleteObject(bucket.getObject('txt1.txt'))
            assertEquals 3, bucket.getSize()

            bucket.deleteObjects([bucket.getObject('txt2.txt'),
                                  bucket.getObject('txt3.txt')])
            assertEquals 1, bucket.getSize()

            bucket.deleteAllObjects()
            assertEquals 0, bucket.getSize()
        } finally {
            bucket.deleteAllObjects()
            bucket.delete()
        }
    }

    @Test
    void testPutFile() {
        def (bucket, bucketName, client) = createContext()

        try {
            assertEquals 0, bucket.getSize()
            bucket.putBulk("$homePath/test-data/dir2/txt5.txt")
            assertEquals 1, bucket.getSize()

            bucket.putBulk(["${homePath}/test-data/dir1/txt1.txt",
                            "${homePath}/test-data/dir1/txt2.txt",
                            "${homePath}/test-data/dir1/txt3.txt"])
            assertEquals 4, bucket.getSize()
        } finally {
            bucket.deleteAllObjects()
            bucket.delete()
        }
    }

    @Test
    void testPutFileFail() {
        def (bucket, bucketName, client) = createContext()

        try {
            shouldFail { bucket.putBulk("$homePath/test-data/dir2/txt10.txt") }
        } finally {
            bucket.deleteAllObjects()
            bucket.delete()
        }
    }

    @Test
    void testPutDirectory() {
        def (bucket, bucketName, client) = createContext()

        try {
            assertEquals 0, bucket.getSize()
            bucket.putBulk("$homePath/test-data/dir3/")
            assertEquals 2, bucket.getSize()
        } finally {
            bucket.deleteAllObjects()
            bucket.delete()
        }
    }

    @Test
    void testGetObject() {
        def (bucket, bucketName, client) = createContext()

        try {
            bucket.putBulk("$homePath/test-data/dir3/", "remote/")
            bucket.getBulk(["remote/txt6.txt", "remote/subdir/txt7.txt"], tmpPath)
            def file = new File("${tmpPath}remote/txt6.txt")
            assert file.exists()
            file = new File("${tmpPath}remote/subdir/txt7.txt")
            assert file.exists()
        } finally {
            bucket.deleteAllObjects()
            bucket.delete()
        }
    }

    @Test
    void testGetDirectory() {
        def (bucket, bucketName, client) = createContext()

        try {
            bucket.putBulk("$homePath/test-data/dir3/", "remote/")
            bucket.getBulk(["remote/"], tmpPath)
            def file = new File("${tmpPath}remote/txt6.txt")
            assert file.exists()
            file = new File("${tmpPath}remote/subdir/txt7.txt")
            assert file.exists()
        } finally {
            bucket.deleteAllObjects()
            bucket.delete()
        }
    }

}
