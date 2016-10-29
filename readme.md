Build
=====
```
./gradlew assemble
./gradlew testClasses
```

Run
===
Since the analysis is written in Java 8
and Soot can only analyze runtime library up to Java 7, additional classpath
for the runtime (jce.jar and rt.jar) should be passed to Soot. For example:

```
java -cp ./build/classes/main PerformanceAnalysis -cp build/classes/test:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/jre/lib/jce.jar Example
```

There is an `Example` class with performance issue in test files.


Limitation
==========
- Native methods and fields are not simulated.
- There is only alias analysis for local variables in backward order. It is unable to reason alias
relationship between object fields, like whether `r0.b.c` and `r1.c` alias. This produces more
false positives than the implementation in paper.

