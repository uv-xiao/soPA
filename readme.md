# PTA (Points-To Analysis)

## Requiring
Java 1.8

maven >= 3.6.0

## Build

Export jar package:

```
mvn package
```

The executable jar file is target/pta-1.0-SNAPSHOT-jar-with-dependencies.jar

## Run with jar package
```
java -jar ./target/pta-1.0-SNAPSHOT-jar-with-dependencies.jar {src} {SomePackage.Main}

e.g.
java -jar ./target/pta-1.0-SNAPSHOT-jar-with-dependencies.jar ./code/ test.Hello
```
{src} is the path to the root directory of tested cases, which should include rt.jar and jce.jar of JDK 1.7. In this project, src should be ./code/

{SomePackage.Main} is the tested Class, which should include a main function. Soot will try to find {src}/{SomePackage.Main}.class for analysis.

The output is in result.txt

## Run directly
You can also try to run without building jar package:

```
mvn compile && mvn exec:java "-Dexec.mainClass=pta.MyPointerAnalysis" "-Dexec.args={src} {SomePackage.Main}"
```
{src} and {SomePackage.Main} are the same as mentioned above.

## Add a new tested Class
If you want to add a new tested class, it should include a main function. You also need to compile it to generate the .class file.

e.g. If you want to add a new class named Hello2 in code/test/:
```
javac ./code/test/Hello2.java -cp ./code/
```