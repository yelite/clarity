Build
=====
```
./gradlew assemble
./gradlew testClasses
```

Run
===
Since the analysis is written in Java 8
and Soot can only analyze up to 7, additional classpath
for java runtime (jce.jar and rt.jar) should be passed to Soot. For example:

```
java -cp ./build/classes/main PerformanceAnalysis -cp build/classes/test:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/jce.jar Example
```

There is an `Example` class with performance issue in test files.

