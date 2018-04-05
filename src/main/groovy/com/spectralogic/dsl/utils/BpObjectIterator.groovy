package com.spectralogic.dsl.utils

import com.spectralogic.ds3client.helpers.pagination.GetObjectsFullDetailsLoader
import com.spectralogic.ds3client.models.DetailedS3Object
import com.spectralogic.dsl.models.BpBucket
import com.spectralogic.dsl.models.BpClient
import com.spectralogic.dsl.models.BpObject

class BpObjectIterator implements Iterator<BpObject> {
    private int pageIndex = 0
    private ArrayList<DetailedS3Object> page
    private GetObjectsFullDetailsLoader objectLoader
    private BpClient client
    private BpBucket bucket

    BpObjectIterator(BpClient client, BpBucket bucket, GetObjectsFullDetailsLoader objectLoader) {
        this.client = client
        this.bucket = bucket
        this.objectLoader = objectLoader
        this.page = this.objectLoader.nextValues
    }

    private void loadNextPage() {
        if (pageIndex >= page.size()) {
            page = objectLoader.nextValues ?: []
            pageIndex = 0
        }
    }

    /* Alternative to next(), returns a list of objects on current page */
    ArrayList<DetailedS3Object> nextPage() {
        loadNextPage()

        def prevIndex = pageIndex
        pageIndex = page.size()

        return page[prevIndex..pageIndex - 1]
    }

    /* Sums page sizes. This is more efficient than counting objects individually */
    Long size() {
        Long size = 0
        while (this.hasNext()) {
            size += this.nextPage().size()
        }

        return size
    }

    @Override
    boolean hasNext() {
        loadNextPage()
        return pageIndex < page.size()
    }

    @Override
    BpObject next() {
        loadNextPage()
        return new BpObject(page[pageIndex++], bucket, client)
    }
}
