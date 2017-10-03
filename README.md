ds3_script
==========

ds3_script is a tool for creating and running scripts using its custom DSL. The DSL is based off of
the [Groovy Language](http://groovy-lang.org/), but implements many custom functions to make writing 
scripts for the BlackPearl easy. The tool also provides a shell that features autocompletion and
script management.

Install
-------


No Shell
-----------------
Using the following command will run the `script.groovy` script without opening the shell.

```$ bpsh script.groovy```

Shell
-----
### Commands
Use ```:help``` to view all of the commands and ```<command> -h``` to see a command's options.

```:clear``` clears the screen of previous lines.

```:exit``` exits the command window, equivalent to `Ctrl-C`.

```:record``` records the current shell session to  a script file to be used for later.

```:execute``` executes the script passed to it.

Example:

```
> args = ['test_bucket']
> :r getBucket
> bucket = client.bucket(args[0])
> :r
> bucket = null
> :e getBucket 'test_bucket'
> assert bucket.name == 'test_bucket'
```

### Shortcuts
You can use tab completion to view available variables, fields, methods, and method parameters. 
Pressing `Esc` and then `Backspace` will delete the current char set. 
`Ctrl-C` will safely exit the shell. 


DSL
--------
These examples will be shown by using the shell. However, you can also write a Groovy script that
uses the DSL and then run it using the `bpsh` command.

### Default variables
Two variables, 'client' and 'environment', are already created and initialized 
if your environment variables are set. 

* client -> BpClient object that represents the client created from environment
variables
* environment -> Environment object that represents set environment variables

#### Clients
```groovy
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
```groovy
bucket = client.createBucket('test_bucket')

assert bucket == client.bucket('test_bucket')
assert client == bucket.client
assert 'test_bucket' == bucket.name
assert client.buckets() == ['test_bucket']

bucket.putBulk('/path/to/example.txt')
bucket.objects()  // shows example.txt object
bucket.empty()    // deletes all objects in bucket

bucket.putBulk('/path/to/example.txt', 'remoteDir')
bucket.objects()  // shows object at remoteDir/example.txt
object = bucket.object('remoteDir/example.txt')
bucket.deleteObjects(object)

bucket.delete()   // bucket must be empty to delete
```

#### Objects
```groovy
object = bucket.object('test_object')
object.metadata
object.size()    // size of object in bytes
object.writeTo('/path/to/destination')
object.delete()  // deletes object from bucket
```

