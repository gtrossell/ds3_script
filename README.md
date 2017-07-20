# ds3_script

Example Script:

```groovy

// endpoint, access id, secret key, .....

client = createBpClient(endpoint, accessid, secretKey, ...)

// get bucket object
bucket = client.bucket('bucket')

// list objects --> ds3.getBucket call
objs = bucket.objects()

// head object
obj = bucket.objects('obj1')

obj.metadata[]

obj.name

obj.owner

// get an object's contents
obj.writeTo(filePath("/my/local/path/file.foo"))

// delete an object
obj.delete()

// create an object
newObject = bucket.createObject("obj1", filePath("/my/local/path/file.foo"), ..) // provide a way to register error handlers

// delete a list of objects
bucket.deleteObjects(objs)


// bulk object put
bucket.putBulk(filePath('/my/local/dir'))
bucket.putBulk(pathList) // --> def void putBulk(List<Path> pathList) 
bucket.putBulk(stringList) // --> def void putBulk(List<String> pathList)

// create bucket
bucket2 = client.createBucket(name: 'bucket2', dataPolicyId: 'aasd:wef:fasd:woi134')

def Path filePath(final String fileName) {
    return Paths.get(fileName)
}

```

## Use-cases
* Connect to Black Pearl
* Connect to multiple Black Pearls
* Create bucket
* Delete bucket
* Put Object to bucket
* Get Object from bucket
* Delete Object from bucket
* Put directory to bucket
* List objects in bucket
* Search for object(s) in bucket
