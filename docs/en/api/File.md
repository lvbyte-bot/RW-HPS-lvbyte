# RW-HPS - File Operations

In RW-HPS, file operations are handled uniformly using `FileUtil`. The root for `FileUtil` is by default at the same level as the jar file, so special management is not required.

For plugins, files obtained through `pluginDataFileUtil` have their root in `plugin/<plugin_name>/`.

## FileUtil

`FileUtil` represents an external file or folder.

### Constructing `FileUtil`

You can construct `FileUtil` using the following methods:

```java
// Gets a file located at the same level as the jar file without creating the file.
FileUtil.getFile("filename");

// Gets a folder located at the same level as the jar file without creating the folder, only creates the directory.
FileUtil.getFolder("foldername");

// Only creates the directory, then gets a file within the created folder.
FileUtil.getFolder("foldername").toFile("filename");

/**
Instances of FileUtil won't perform any action or create directories and files.
If you need to create a directory first and then a file, use FileUtil.toFolder("foldername").toFile("filename").
If you need to enter multiple directories, use FileUtil.toFolder("foldername").toFolder("subfoldername").
Files are created only when using FileUtil().read/write.
Note:
FileUtil.toFolder's initial directory is either the Server.jar directory or the directory specified by Main parameters.
toFolder only functions to enter the specified directory.
*/
// Gets a temporary file.
FileUtil.getTempFile("filename");

// Gets a temporary directory.
FileUtil.getTempDirectory("foldername");

// Creates the directory and attempts to create the file.
new FileUtil().mkdir();

// Attempts to create the file.
new FileUtil().createNewFile();
```

### Example Usage:

#### Creating and Writing to a File

```java
FileUtil file = FileUtil.getFile("example.txt");
file.write("This is a test content.");
```

#### Reading from a File

```java
FileUtil file = FileUtil.getFile("example.txt");
String content = file.read();
System.out.println(content);
```

#### Creating a Folder and a File within It

```java
FileUtil folder = FileUtil.getFolder("exampleFolder");
FileUtil file = folder.toFile("example.txt");
file.write("This file is inside exampleFolder.");
```

#### Handling Temporary Files

```java
FileUtil tempFile = FileUtil.getTempFile("temp.txt");
tempFile.write("This is temporary content.");
```

#### Creating Nested Directories

```java
FileUtil nestedFolders = FileUtil.toFolder("folder1").toFolder("folder2").toFile("nestedFile.txt");
nestedFolders.write("This file is inside folder1/folder2.");
```

By utilizing `FileUtil`, RW-HPS ensures consistent and simplified file operations within the server environment.