buckets = client.bucketNames().collect { client.getBucket(it) }
buckets.each { it.deleteAllObjects(); it.delete() }
assert 0 == client.size
