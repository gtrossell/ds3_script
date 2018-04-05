package com.spectralogic.dsl.utils

import com.spectralogic.ds3client.helpers.pagination.GetObjectsFullDetailsLoader
import com.spectralogic.dsl.helpers.Globals
import com.spectralogic.dsl.models.BpBucket
import com.spectralogic.dsl.models.BpClient
import com.spectralogic.dsl.models.BpObject

/** Iterable for BpObjects loaded by GetObjectsFullDetailsLoader paging */
class BpObjectIterable<T> implements Iterable<T> {
    private Boolean valueRetrieved = false
    private GetObjectsFullDetailsLoader objectLoader
    private BpClient client
    private BpBucket bucket

    BpObjectIterable(BpClient client, BpBucket bucket, String folder='') {
        this.objectLoader = new GetObjectsFullDetailsLoader(client, bucket.name, folder, true, Globals.OBJECT_PAGE_SIZE, 1)
        this.client = client
        this.bucket = bucket
    }

    @Override
    BpObjectIterator iterator() {
        if (this.valueRetrieved) {
            throw new Exception("Iterator was already retrieved")
        }
        this.valueRetrieved = true

        return new BpObjectIterator(this.client, this.bucket, this.objectLoader)
    }
}
