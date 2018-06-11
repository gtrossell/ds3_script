# Installing bpsh

DS3 Script is run using bpsh (BlackPearl Shell). It runs on Linux, OSX, and Windows and requires Java 8+. Versions of 
Java 9+ will throw warnings because bpsh is built on the [Groovy](http://www.groovy-lang.org/) language, but we haven't
seen any bugs come from this other than the warnings. There are currently installers for Debian and Red Hat. Other OSs 
will require you to copy binaries or build from source. If you want to build from source see 
[BUILD.md](https://github.com/SpectraLogic/ds3_script/blob/master/BUILD.md).

Release Page: https://github.com/SpectraLogic/ds3_script/releases

Before running bpsh, you may want to set the environment variables for your default BlackPearl client. These are the 
same as the [DS3 Java CLI](https://github.com/SpectraLogic/ds3_java_cli#user-content-usage): 
`DS3_ENDPOINT`, `DS3_ACCESS_KEY`, and `DS3_SECRET_KEY`. This will allow bpsh to create the variable `client` on start.

## Windows

1. Check that Java version is 1.8+: `java -version`
2. Download latest binaries from the [releases page](https://github.com/SpectraLogic/ds3_script/releases)
3. Unzip/tar into the directory you want bpsh to be installed
4. Add the `/bin` directory to your Path
5. Check that you installed the correct version with `bpsh -v`

## Linux

### Debian

[Installer Page](https://bintray.com/spectralogic/bpsh_deb/bpsh)

```
$ echo "deb [trusted=yes] https://dl.bintray.com/spectralogic/bpsh_deb stable main" | sudo tee -a /etc/apt/sources.list
$ sudo apt update
$ sudo apt-get install bpsh
$ bpsh -v
```

### Red Hat

[Installer Page](https://bintray.com/spectralogic/bpsh_rpm/bpsh)

```
$ wget https://bintray.com/spectralogic/bpsh_rpm/rpm -O bintray-spectralogic-bpsh_rpm.repo
$ sudo mv bintray-spectralogic-bpsh_rpm.repo /etc/yum.repos.d/
$ sudo yum install bpsh
$ bpsh -v
```

## OSX

There will be a [Hombrew](https://brew.sh/) installer soon.


## Java 9+ notes
The warning given in Java 9+ will not affect bpsh usability. The issue is currently 
[being worked on by the Groovy developers](https://issues.apache.org/jira/browse/GROOVY-8339). Some solutions to silence
these warnings can be found [here](https://github.com/gradle/gradle/issues/2995).
