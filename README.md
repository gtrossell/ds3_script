ds3_script
==========

DS3 Script is a JVM scripting language for interfacing with the BlackPearl and is run using the bpsh executable. The DSL 
is based on the [Groovy Language](http://groovy-lang.org/), but implements many custom objects and functions to make 
writing scripts for the BlackPearl easy. It's recommended to get comfortable with Groovy's 
[syntax & features](http://groovy-lang.org/syntax.html) first, but note that bpsh does not ask the user to use 
`def` to define new variables. bpsh also provides a shell that features autocompletion, debugging, logging,
and script management.

Releases
--------
Official release binaries are hosted on Github. Please add an issue to the issue tracker to report bugs.

[Releases](https://github.com/SpectraLogic/ds3_script/releases)

[Install instructions](https://github.com/SpectraLogic/ds3_script/blob/master/INSTALL.md)

[Build instructions](https://github.com/SpectraLogic/ds3_script/blob/master/BUILD.md)

[Issue tracker](https://github.com/SpectraLogic/ds3_script/issues)

Shell
-----
### External Commands
`$ bpsh -h` prints available options.

`$ bpsh -v` prints the version number.

`$ bpsh -d` enables debugging mode, traces will be printed.

`$ bpsh -l <log directory>` enables logging to specified directory. Once set, the log directory is saved for the future.

`$ bpsh script` will run the `script.groovy` script without opening the shell.

### Internal Commands
Use `:help` (`:h`) to view all of the commands that can be used inside the shell and ```<command> -h``` to see a specific 
command's help page.

`:clear` (`:c`) clears the screen of previous lines.

`:quit` (`:q`) exits the command window, equivalent to `Ctrl-C`.

`:record` (`:r`) records the current shell session to  a script file to be used for later. If an exception is thrown during
the recording, that line is not recorded. *this may change in the future. 

`:execute` (`:e`) executes, lists, and deletes scripts.

`:log` (`:l`) sets the log directory and enables/disables logging .

Example:

```
> args = ['test_bucket']
> :r getBucket
> bucket = client.getBucket(args[0])
> :r
> bucket = null
> :e getBucket test_bucket
> assert bucket.name == 'test_bucket'
```

### Shortcuts
You can use tab completion to view available variables, fields, methods, and method parameters. 

`Esc-Backspace` will delete the current character set. 

`Ctrl-C` will safely exit the shell. 

DSL
--------
These examples will be shown by using the shell. However, you can also write a Groovy script that
uses the DSL and then run it using the `bpsh` command (ex: `$ bpsh CreateBucket.groovy`). See the 
[samples directory](https://github.com/SpectraLogic/ds3_script/tree/master/samples) for script examples.

### Default variables
Two variables, `client` and `environment`, are already created and initialized 
if your environment variables are set. [Variables to set.](https://github.com/SpectraLogic/ds3_java_cli#user-content-usage)

* `client` -> BpClient object that represents the client created from environment
variables
* `environment` -> Environment object that represents set environment variables for endpoint, access key, and secret key

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
 
assert bucket == client.getBucket('test_bucket')
assert client == bucket.client
assert 'test_bucket' == bucket.name
assert client.buckets() == ['test_bucket']
 
bucket.putBulk('/path/to/example.txt', 'remoteDir/')
bucket.getAllObjects().collect() // list all objects in bucket
bucket.getBulk(['remoteDir/example.txt'], "./downloads/")
// or get by directory
bucket.getBulk(['remoteDir/'], "./downloads/")
bucket.deleteAllObjects()
assert 0 == bucket.size

bucket.putBulk('/path/to/example.txt', 'remoteDir/')
object = bucket.getObject('remoteDir/example.txt')
bucket.deleteObject(object)
assert 0 == bucket.objects().size()

bucket.delete()             // bucket must be empty to delete
```

#### Objects
```groovy
object = bucket.object('test_object')
object.exists
object.metadata
object.writeTo('/path/to/destination')
object.delete()  // deletes object from bucket
```

