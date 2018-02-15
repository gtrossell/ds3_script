package com.spectralogic.dsl.models

import com.spectralogic.ds3client.Ds3ClientImpl
import com.spectralogic.ds3client.models.bulk.Ds3Object
import com.spectralogic.ds3client.models.Contents
import com.spectralogic.ds3client.models.User

/** Represents a BlackPearl Object */
class BpObject {
    private final BpBucket bucket
    private final Ds3ClientImpl client
    private final User owner
    final Map metadata
    final String name
    final Integer size

    BpObject(Contents contents, BpBucket bucket, Ds3ClientImpl client) {
        this.name = contents.getKey()
        this.size = contents.getSize()
        this.owner = contents.getOwner()
        this.bucket = bucket
        this.client = client
        this.metadata = [
                name      : this.name,
                size      : this.size,
                owner     : this.owner,
                bucketName: this.bucket.name
        ]
    }

    /**
     * Deletes the object from the BP
     */
    void delete() {
        bucket.deleteObjects(this)
    }

    /**
     * Uses BpBucket.getBulk
     * @param pathStr directory to write object to
     */
    void writeTo(String pathStr) {
        bucket.getBulk([name], pathStr)
    }

}
