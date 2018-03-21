package com.spectralogic.dsl.models

import com.spectralogic.ds3client.Ds3ClientImpl
import com.spectralogic.ds3client.commands.HeadObjectRequest
import com.spectralogic.ds3client.commands.HeadObjectResponse
import com.spectralogic.ds3client.models.DetailedS3Object
import com.spectralogic.ds3client.models.User

/** Represents a BlackPearl Object */
class BpObject {
    private final BpBucket bucket
    private final Ds3ClientImpl client
    private final User owner
    final String name
    Long size
    Map metadata

    BpObject(DetailedS3Object object, BpBucket bucket, BpClient client) {
        this.bucket = bucket
        this.client = client
        this.name = object.name
        def user = new User()
        user.displayName = object.owner
        // TODO? owner id not given in object
        this.owner = user
    }

    BpObject(String name, BpBucket bucket, BpClient client) {
        // TODO: client.headObject to make sure object exists
        this.bucket = bucket
        this.client = client
        this.name = name
        this.owner = new User()
    }

    Long getSize() {
        return this.client.headObject(new HeadObjectRequest(this.bucket.name, this.name)).objectSize
    }

    Map getMetadata() {
        return [
                name      : this.name,
                size      : this.getSize(),
                owner     : this.owner,
                bucketName: this.bucket.name
        ]
    }

    Boolean exists() {
        switch (this.client.headObject(new HeadObjectRequest(this.bucket.name, this.name)).status) {
            case HeadObjectResponse.Status.EXISTS:
                return true
            default:
                return false
        }
    }

    /**
     * Deletes the object from the BP
     */
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
