package com.spectralogic.dsl.models

import com.spectralogic.ds3client.Ds3ClientImpl
import com.spectralogic.ds3client.commands.HeadObjectRequest
import com.spectralogic.ds3client.commands.HeadObjectResponse
import com.spectralogic.ds3client.models.DetailedS3Object
import com.spectralogic.ds3client.networking.Metadata

/** Represents a BlackPearl Object */
class BpObject {
    private final BpBucket bucket
    private final Ds3ClientImpl client
    final String name
    final Long size
    final Map metadata
    final Boolean exists

    BpObject(DetailedS3Object object, BpBucket bucket, BpClient client) {
        this.bucket = bucket
        this.client = client
        this.name = object.name

        this.size = -1
        this.metadata = [:]
        this.exists = false
    }

    BpObject(String name, BpBucket bucket, BpClient client) {
        this.bucket = bucket
        this.client = client
        this.name = name

        this.size = -1
        this.metadata = [:]
        this.exists = false
    }

    Long getSize() {
        return this.client.headObject(new HeadObjectRequest(this.bucket.name, this.name)).objectSize
    }

    Map<String,List<String>> getMetadata() {
        def metadata = this.client.headObject(new HeadObjectRequest(this.bucket.name, this.name)).metadata

        return metadata.keys().collectEntries { [(it) : metadata.get(it)] }
    }

    Boolean isExists() {
        switch (this.client.headObject(new HeadObjectRequest(this.bucket.name, this.name)).status) {
            case HeadObjectResponse.Status.EXISTS:
                return true
            default:
                return false
        }
    }

    /** Delete object from the BlackPearl */
    void delete() {
        bucket.deleteObject(this)
    }

    /**
     * Uses BpBucket.getBulk
     * @param pathStr directory to write object to
     */
    void writeTo(String pathStr) {
        bucket.getBulk([name], pathStr)
    }

    String toString() {
        return "name: $name"
    }

}
