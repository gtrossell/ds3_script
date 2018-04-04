package com.spectralogic.dsl.utils

import com.spectralogic.ds3client.helpers.pagination.GetObjectsFullDetailsLoader
import com.spectralogic.ds3client.models.DetailedS3Object
import com.spectralogic.dsl.helpers.Globals
import com.spectralogic.dsl.models.BpBucket
import com.spectralogic.dsl.models.BpClient
import com.spectralogic.dsl.models.BpObject

/** Iterable for BpObjects loaded by GetObjectsFullDetailsLoader paging */
class BpObjectIterable<T> implements Iterable<T> {
    private Boolean valueRetrieved = false
    private GetObjectsFullDetailsLoader objectLoader
    private ArrayList<DetailedS3Object> page
    private int pageIndex = 0
    private BpClient client
    private BpBucket bucket

    BpObjectIterable(BpClient client, BpBucket bucket, String folder='') {
        this.objectLoader = new GetObjectsFullDetailsLoader(client, bucket.name, folder, true, Globals.OBJECT_PAGE_SIZE, 1)
        this.page = this.objectLoader.nextValues
        this.client = client
        this.bucket = bucket
    }

    @Override
    Iterator<BpObject> iterator() {
        if (this.valueRetrieved) {
            throw new Exception("Iterator was already retrieved")
        }
        this.valueRetrieved = true

        return new Iterator<BpObject>() {
            private void loadNextPage() {
                if (pageIndex >= page.size()) {
                    page = objectLoader.nextValues ?: []
                    pageIndex = 0
                }
            }

            /* Alternative to next(), returns a list of objects on current page */
            ArrayList<DetailedS3Object> nextPage() {
                def currPage = page
                page = objectLoader.nextValues
                return currPage
            }

            /* Sums page sizes. This is more efficient than counting objects is BpBucket to get size */
            Long size() {
                Long size = 0
                while (page.size() == Globals.OBJECT_PAGE_SIZE) {
                    size += Globals.OBJECT_PAGE_SIZE
                    page = objectLoader.nextValues
                }
                size += page.size()
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
    }
}
