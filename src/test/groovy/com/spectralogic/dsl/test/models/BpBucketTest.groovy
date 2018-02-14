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
        shouldFail { client.bucket(bucketName) }
    }

    @Test
    void testDeleteObjects() {
        def (bucket, bucketName, client) = createContext()

        try {
            assertEquals 0, bucket.size()
            bucket.putBulk(["${homePath}/test-data/dir1/txt1.txt",
                            "${homePath}/test-data/dir1/txt2.txt",
                            "${homePath}/test-data/dir1/txt3.txt",
                            "${homePath}/test-data/dir1/txt4.txt"])
            assertEquals 4, bucket.size()

            bucket.deleteObject(bucket.object('txt1.txt'))
            assertEquals 3, bucket.size()

            bucket.deleteObjects([bucket.object('txt2.txt'),
                                  bucket.object('txt3.txt')])
            assertEquals 1, bucket.size()

            bucket.deleteAllObjects()
            assertEquals 0, bucket.size()
        } finally {
            bucket.deleteAllObjects()
            bucket.delete()
        }
    }

    @Test
    void testPutFile() {
        def (bucket, bucketName, client) = createContext()

        try {
            assertEquals 0, bucket.size()
            bucket.putBulk("$homePath/test-data/dir2/txt5.txt")
            assertEquals 1, bucket.size()

            bucket.putBulk(["${homePath}/test-data/dir1/txt1.txt",
                            "${homePath}/test-data/dir1/txt2.txt",
                            "${homePath}/test-data/dir1/txt3.txt"])
            assertEquals 4, bucket.size()
        } finally {
            bucket.deleteAllObjects()
            bucket.delete()
        }
    }

    @Test
    void testPutDirectory() {
        def (bucket, bucketName, client) = createContext()

        try {
            assertEquals 0, bucket.size()
            bucket.putBulk("$homePath/test-data/dir3/")
            assertEquals 2, bucket.size()
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

}
