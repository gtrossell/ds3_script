package com.spectralogic.dsl.utils

class IteratorWrapper<T> implements Iterable<T> {
    private final Iterator<T> iter
    private Boolean valueRetrieved = false

    IteratorWrapper(Iterator<T> iterator) {
        this.iter = iterator
    }

    @Override
    Iterator<T> iterator() {
        if (this.valueRetrieved) {
            throw new Exception("Iterator was already retrieved")
        }
        this.valueRetrieved = true

        return this.iter
    }
}
