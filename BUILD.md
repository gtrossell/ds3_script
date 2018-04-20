# Building bpsh

To build bpsh (the ds3_script shell), you will need to have 
[Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) installed. Once Java 8 is installed, you 
can use [git](https://git-scm.com/) to download the source or download the source zip from Github.

Once the source code is copied to your local system, cd into the root directory. To build bpsh run the command 
`./gradlew build -x test` from the command line for Unix systems. On Windows the command is `gradlew.bat build -x test`.

Once built, cd into the `build/distributions` directory and unzip `bpsh-X.X.zip` to wherever you want bpsh to reside.
Add the directory to your path and you can run bpsh using the `bpsh` command in the terminal. Optionally, you can also 
run bpsh if you cd into `bpsh-X.X/bin` and run `./bpsh` on Unix systems or `bpsh.bat` on Windows systems.

Before running bpsh, you may want to set the environment variables for your default BlackPearl client. These are the 
same as the [DS3 Java CLI](https://github.com/SpectraLogic/ds3_java_cli#user-content-usage): `DS3_ENDPOINT`, 
`DS3_ACCESS_KEY`, and `DS3_SECRET_KEY`. This will allow bpsh to create the variable `client` on start.

Note: Don't use the `gradlew run` command to build and run bpsh.

## Unix/bash example
```
$ cd
$ git clone https://github.com/SpectraLogic/ds3_script.git
$ cd ds3_script/
$ ./gradlew build -x test
$ unzip build/distributions/bpsh-0.5.zip -d ~/
$ cd
$ ./bpsh-0.5/bin/bpsh
```

