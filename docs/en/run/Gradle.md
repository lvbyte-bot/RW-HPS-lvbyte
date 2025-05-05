# RW-HPS - Gradle

## Gradle

### What is Gradle

Gradle is a project automation tool that builds upon the concepts of Apache Ant and Apache Maven. It uses a Groovy-based domain-specific language to declare project settings, instead of the traditional XML. Currently, it supports languages like Java, Groovy, and Scala, with plans to support more languages in the future.

### How to Use Gradle

1. Gradle runs on the JVM, so you need to have the JDK and JRE installed. It is recommended to use Java 11, as Java is backward compatible.
2. Download the Gradle package from the official Gradle website. The page provides two methods: manual installation and installation via a script. I generally prefer manual installation as it makes future cleanup easier.
3. After downloading the package, extract it and configure the environment variables. If you've manually installed the JDK before, you should be familiar with configuring environment variables. The method to configure environment variables varies across different platforms.

## Compiling with Gradle

### Initialization

1. Open Terminal  
   Open Cmd or PowerShell in the directory you need.
2. Start Compilation  
   Input the following command in the terminal:

```bash
./gradlew jar
```

Wait until the process is completed.

3. Usage  
The compiled Server Jar can be found in the `Server-All/build/libs` directory.

**For more tutorials, please refer to [Google](https://google.com), [Baidu](https://baidu.com), or [Bing](https://bing.com).**