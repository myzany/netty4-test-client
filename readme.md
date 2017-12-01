### Gradle build

```
./gradlew clean build -x test
```

### Release build

```
[zany@titan netty4-test-client]$ ./release.sh

BUILD SUCCESSFUL in 3s
6 actionable tasks: 6 executed

[zany@titan netty4-test-client]$ cd _release/
[zany@titan _release]$ ls -al
합계 24
drwxr-xr-x 5 zany mysql 4096 2017-12-01 18:55 .
drwxr-xr-x 8 zany mysql 4096 2017-12-01 18:55 ..
drwxr-xr-x 2 zany mysql 4096 2017-12-01 18:55 config
drwxr-xr-x 2 zany mysql 4096 2017-12-01 18:55 lib
drwxr-xr-x 2 zany mysql 4096 2017-12-01 18:55 payload
-rwxr-xr-x 1 zany mysql 1523 2017-12-01 18:39 test.run
```

### Usage

```
[zany@titan _release]$ ./test.run --help

Linux 2.6.32-696.13.2.el6.x86_64 #1 SMP Thu Oct 5 21:22:16 UTC 2017
usage: Netty4 Test Client
 -?,--help           show help.
 -h,--host <arg>     host ip address.
 -l,--loop <arg>     loop count.
 -p,--port <arg>     host port number.
 -r,--ramp <arg>     ramp up seconds.
 -t,--thread <arg>   thread count.
```
