ds3_script
==========

ds3_script is a tool for creating and running scripts using its custom DSL. The DSL is based off of
the [Groovy Language](http://groovy-lang.org/), but implements many custom functions to make writing 
scripts for the BlackPearl easy. The tool also provides a shell that features autocompletion and
script management.

Install
-------
Build the source using Gradle, unzip the distribution, and add the bin directory to the path.

No Shell
-----------------
Using the following command will run the `script.groovy` script without opening the shell.

```$ bpsh script.groovy```

Shell
-----
### Commands
Use ```:help``` to view all of the commands and ```<command> -h``` to see a command's options.

```:clear``` clears the screen of previous lines.

```:quit``` exits the command window, equivalent to `Ctrl-C`.

```:record``` records the current shell session to  a script file to be used for later. If an exception is thrown during
the recording, that line is not recorded. *this may change in the future. 

```:execute``` executes the script passed to it.

```:log``` sets the log directory and enables/disables logging 

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
`Esc-Backspace` will delete the current char set. 
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
```

#### Buckets
```groovy
bucket = client.createBucket('test_bucket')
 
assert bucket == client.bucket('test_bucket')
assert client == bucket.client
assert 'test_bucket' == bucket.name
assert client.buckets() == ['test_bucket']
 
bucket.putBulk('/path/to/example.txt')
bucket.objects()            // shows example.txt object
bucket.getBulk(['example.txt'], "./downloads/")
bucket.deleteAllObjects()
assert 0 == bucket.objects().size()
 
bucket.putBulk('/path/to/example.txt', 'remoteDir/')
bucket.objects()            // shows object at remoteDir/example.txt
object = bucket.object('remoteDir/example.txt')
bucket.deleteObjects(object)
assert 0 == bucket.objects().size()
 
bucket.delete()             // bucket must be empty to delete
```

#### Objects
```groovy
object = bucket.object('test_object')
object.metadata
object.writeTo('/path/to/destination')
object.delete()  // deletes object from bucket
```

