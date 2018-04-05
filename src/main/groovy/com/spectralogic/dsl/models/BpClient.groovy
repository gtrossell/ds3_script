package com.spectralogic.dsl.models

import com.spectralogic.ds3client.commands.spectrads3.PutBucketSpectraS3Request
import com.spectralogic.ds3client.networking.NetworkClient
import com.spectralogic.ds3client.commands.GetServiceRequest
import com.spectralogic.ds3client.Ds3ClientImpl

/** Represents a BlackPearl Client */
class BpClient extends Ds3ClientImpl {
    final Long size
    final boolean https

    BpClient(NetworkClient netClient) {
        super(netClient)

        this.size = -1
        this.https = false
    }

    /** @return BpBucket of getBucket with given name  */
    BpBucket getBucket(String bucketName) {
        return new BpBucket(bucketName, this)
    }

    /** @return the names of the buckets  */
    List<String> bucketNames() {
        def response = this.getService(new GetServiceRequest())
        return response.getListAllMyBucketsResult().getBuckets().collect { it.getName() }
    }

    /** @return newly created BpBucket  */
    BpBucket createBucket(String name, String dataPolicyId = "") {
        putBucketSpectraS3(new PutBucketSpectraS3Request(name).withDataPolicyId(dataPolicyId))
        return getBucket(name)
    }

    boolean isHttps() {
        return this.connectionDetails.https
    }

    Long getSize() {
        return this.bucketNames().size()
    }

    String toString() {
        def details = this.netClient.getConnectionDetails()
        "endpoint: ${details.getEndpoint()}, " +
                "access_key: ${details.getCredentials().getClientId()}"
    }

}
