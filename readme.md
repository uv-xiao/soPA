# SoPA(Soot-based Pointer Analysis)

## Requiring
Java 1.8

maven >= 3.6.0

## Features
* Anderson
* Field-sensitivity

## Design

### Field-sensitivity
set = {(x, {$r1, $r2, ...})}
In soot, $rx denotes an object. I use `object2line` to find the id of each object.
We ask that set includes ($rx, {$rx}).
For field-sensitivity, also feed ($rx.f, {}) into the set.

### inter-procedure
Now the algorithm can handle function calling without recursion partly.

Each time the given program calling a function, our algorithm will generate a new `Algorithm` Object to calculate the result of the called function, and use store `callstack` detect recursion.




## Build, Run & Test

From TA
### Export jar package:

```
mvn package
```

The executable jar file is target/pta-1.0-SNAPSHOT-jar-with-dependencies.jar

### Run with jar package
```
java -jar ./target/pta-1.0-SNAPSHOT-jar-with-dependencies.jar {src} {SomePackage.Main}

e.g.
java -jar ./target/pta-1.0-SNAPSHOT-jar-with-dependencies.jar ./code/ test.Hello
```
{src} is the path to the root directory of tested cases, which should include rt.jar and jce.jar of JDK 1.7. In this project, src should be ./code/

{SomePackage.Main} is the tested Class, which should include a main function. Soot will try to find {src}/{SomePackage.Main}.class for analysis.

The output is in result.txt

### Run directly
You can also try to run without building jar package:

```
mvn compile && mvn exec:java "-Dexec.mainClass=sopa.MyPointerAnalysis" "-Dexec.args={src} {SomePackage.Main}"
```

```
mvn compile && mvn exec:java "-Dexec.mainClass=sopa.MyPointerAnalysis" "-Dexec.args=code test.FieldSensitivity"
```
{src} and {SomePackage.Main} are the same as mentioned above.

### Add a new tested Class
If you want to add a new tested class, it should include a main function. You also need to compile it to generate the .class file.

e.g. If you want to add a new class named Hello2 in code/test/:
```
javac ./code/test/Hello2.java -cp ./code/
```
