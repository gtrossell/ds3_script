bucket = client.getBucket('my_bucket')

/* delete objects in 'dir/subdir/' */
bucket.deleteObjects(bucket.getAllObjects.findAll { it.name.startsWith('dir/subdir/') })

/* delete objects with 0 bytes */
bucket.deleteObjects(bucket.getAllObjects.findAll { it.size == 0 })

/* see all objects in bucket */
println bucket.getAllObjects().collect()
/* or */
println bucket.getAllObjects().collect { it.name }

/* delete all objects in bucket */
bucket.deleteAllObjects()
assert bucket.empty
