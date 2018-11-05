# CS

## Overview
Contains simple code to read Json file and write to DB (HSQL by default)

There are 2 implementations PlainJacksonJdbcProcessor and JacksonJpaProcessor.

Both use Lombok for boilerplate code generation.

### PlainJacksonJdbcProcessor
This is a simpler implementation that uses Jackson and Jdbc without Spring overhead. (Note, the gradle build was not optimized for multi builds, so for now it just produces a single uber jar file with the dependencies of all solutions)
* To invoke call. ```java -jar build\libs\demo-0.0.1-SNAPSHOT.jar <jsonFilename>```
* Uses Jackson to read and parse the json file. It uses the Jackson Streaming fuctionality in order to reduce memory overhead for handling large files.
* JDBC connection details are loaded from the application.properties file.

### JacksonJpaProcessor
* This implementation was build using SpringBoot and uses the Spring Data to write to the DB. It reuses the Jackson Streaming file handling of the first implementation.
* To invoke call. ```java -cp build\libs\demo-0.0.1-SNAPSHOT.jar  -Dloader.main=com.example.demo.SpringBootProcessor org.springframework.boot.loader.PropertiesLauncher src\test\resources\test.json```
* Uses Jackson to read and parse the json file. It uses the Jackson Streaming fuctionality in order to reduce memory overhead for handling large files.
* Connection details are loaded from the application.properties file.
* N.B. By default HSQL db is configured to write in memory mode (despite setting the url to file mode). Therefore we had to override more configurasitions to get it to write to file.

### Todo Streaming Api
I found some interesting links for a multithreaded reactive solution here
https://www.nurkiewicz.com/2017/09/streaming-large-json-file-with-jackson.html
If I have some spare time I will try to implement this solution

## Getting Started/ Sample usage
```git clone https://github.com/khylo/cs
cd cs
# Run the unit tests
gradlew test
# Build an executable jar file
gradlew build
#Run the PlainJDBC app
java -jar build\libs\demo-0.0.1-SNAPSHOT.jar src\test\resources\test.json
#Run SpringBoot
java -cp build\libs\demo-0.0.1-SNAPSHOT.jar  -Dloader.main=com.example.demo.SpringBootProcessor org.springframework.boot.loader.PropertiesLauncher src\test\resources\test.json

```

