# Examples
These are examples for how to use the ds3 script tool. The tool includes a DSL
specifically for use with the BlackPearl and a set of commands used for creating, running, and managing scripts within the tool.

## DSL

### Default variables
Two variables, 'client' and 'environment', are already created and initialized 
if your environment variables are set. 

* client -> BpClient object that represents the client created from environment
variables
* environment -> Environment object that represents set environment variables

#### Clients
```
// create client from environment variables
client = createBpClient()
// with https
client = createBpClient(https=true)
// using arguments
client = createBpClient(endpoint, accessKey, secretKey)

// create client from custom environment
envVars = ['DS3_ENDPOINT': '', 'DS3_ACCESS_KEY': '', 'DS3_SECRET_KEY': '']
customEnv = new Environment(envVars)
client = createBpClient(environment=customEnv)
```

#### Buckets
```
bucket = client.createBucket('test_bucket')

assert bucket == client.bucket('test_bucket')
assert client == bucket.client
assert 'test_bucket' == bucket.name
assert client.buckets() == ['test_bucket']

bucket.putBulk('/path/to/example.txt')
bucket.objects() // shows example.txt object
bucket.empty() // deletes all objects in bucket

bucket.putBulk('/path/to/example.txt', 'remoteDir')
bucket.objects() // shows object at remoteDir/example.txt
object = bucket.object('remoteDir/example.txt')
bucket.deleteObjects(object)

bucket.delete() // bucket must be empty to delete
```

### Objects
```
object = bucket.object('test_object')
object.metadata
object.size() // size of object in bytes
object.writeTo('/path/to/destination')
object.delete() // deletes object from bucket
```

## Commands
Use ```<command> -h``` to see all options

### :record
Make a script called 'getBucket' that creates a bucket variable after passing
the bucket name in as a argument
```
> args = ['test_bucket']
> :r getBucket
> bucket = client.bucket(args[0])
> :r
```
When recording, you can record your current environment variables to always be
used by the script, specify a location for the script to record to, and give a 
description that will be recorded to the script's title comment

### :execute
Run the above script
```
> :e getBucket 'test_bucket'
> assert bucket.name == 'test_bucket'
```
This command also allows you to list and delete scripts

### :help
