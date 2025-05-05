# RW-HPS Start Server

## A.基本配置要求:

### 1.JVM 环境要求

- JVM：最低版本： <font style="color:red;font-weight:bold">~~Java 8(因为HPS架构升级，现已不再可用)~~</font> JDK21

### 2.如何获取JDK?

> 您需要下载 JDK21以获得：
- 1.**Windows** 下载 JDK21 : </br> [ORACLE JDK21 Windows x64 Installer](https://download.oracle.com/java/21/archive/jdk-21.0.2_windows-x64_bin.exe)
- 2.对于**Debian的发行版系统** </br> 
Debian deb包 : </br> [ORACLE JDK21 x64 Debian Package](https://download.oracle.com/java/21/archive/jdk-21.0.2_linux-x64_bin.deb) </br> 或是通过软件源安装 </br>
```
sudo apt-get update -y #更新软件源
sudo apt-get install openjdk-21-jdk -y #安装JDK21
```
- 3.不建议使用**CentOS**运行， CentOS项目已于2021年12月31日停止，不再有官方支持。


# B.运行方案

## 下载编译好的服务端

通过 [Release](https://github.com/deng-rui/RW-HPS/releases) 下载

### Windows 运行方法

- 1.把下载好的**服务端**(**Server-All.jar**)存放到你喜欢的，能被你自己找到的任意文件夹。
- 2.有存放服务端的文件夹里右键鼠标，点击**在终端中打开**
- 3.键入以下指令:
```bash
java -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 -jar Server.jar
# TODO:如果乱码或者无法执行，请使用下面的指令。
java -jar Server.jar
```
- 4.对于懒惰每次启动都要打开终端的人类，建议在存放服务端的根目录创建一个后缀为.bat的start.bat文件，在文件里写入上面的其中一条指令，过后每次启动只需要点击该文件就好了

### Debian/Ubuntu运行方法
- 1.通过 wget 下载最新版本的服务端
```bash
wget https://github.com/deng-rui/RW-HPS/releases/download/3.0.0-M5/Server-All.jar
```
- 2.通过`mv Server_All.jar <目标文件夹>`移动服务端到你喜欢的文件夹里
- 3.用和Windowss上一样的指令执行服务端：
```bash
java -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 -jar Server.jar
# TODO:如果乱码或者无法执行，请使用下面的指令。
java -jar Server.jar
```

## RW-HPS运行的常见问题:

### Q.1.:**corroding: nativePollOnce:100,0**

<img src="../img/Question.png"></img>

#### 此问题分析：如果长时间卡在这个位置, 那么就是缺少一些依赖包，或者版本跨度太大导致不兼容 ,因此你会遇到该错误。

<img src="../img/Question2.png"></img>
#### 解决策略：关闭服务器, 然后删掉图中的 `data` 文件夹, 再重新启动服务器
---

### Q.2.:**Error: A JNI error has occurred, please check your installation and try again**

<img src="../img/Question3.png"></img>

#### 此问题分析：JDK版本低于21，本问题主要是 RW-HPS 使用 Java11 编译, 导致无法使用 Java8 运行。

<img src="../img/Question4.png"></img>

#### 解决策略：升级到JDK11,具体参考上面的JDK配置。正确的JDK11在你输入:

```bash
java -version
```

显示下方的图即代表你已经成功升级到JDK21:

<img src="https://img.lu/upload/b1c58d5f11dee00543aee.png"></img>

### Q.3.:**Not D!!!**

<img src="../img/Question6.png"></img>

#### 问题分析：D的参数不被支持(可能是被.分割了) ，因此需要使用指令:

```bash
java -D"file.encoding=UTF-8" -jar Server.jar
```

#### 解决策略：使用上方提供的指令

### Q.4.:**java.io.IOException: Prolem reading font data**

<img src="../img/Question7.png"></img>

#### 问题分析：如果您使用的 Linux, 那么是缺少 Font 依赖:

#### 解决策略：手动安装 `fontconfig`
  
**Ubuntu** : ```apt-get install fontconfig```
---
## 3.手动编译最新的测试版本

### 我不知道Gradle如何使用请移步

[Gradle教程](Gradle.md)

1.需要安装Git Java21 Screen(或许可以使用你喜欢的保活方式)

### A.Ubuntu使用

```bash  
sudo apt update
sudo apt-get install git openjdk-21-jdk screen -y  
```

2.同步存储库
> 根据个人喜好

```bash
HTTPS  
git clone https://github.com/deng-rui/RW-HPS.git
``` 

```bash  
SSH
git clone git@github.com:deng-rui/RW-HPS.git  
```

3.开始编译最新版本
在命令行输入

```bash
./gradlew jar
```

等待完毕即可

4.使用  
在目录 `Server-All/build/libs` 下即可获得编译好的Server Jar

5.运行  
在你喜欢的目录下运行jar

```bash
java -Djava.net.preferIPv4Stack=true -jar Server.jar
```

但是这样会在SSH断开后被关闭 那么我们就使用上文的Screen

## 4.使用Screen

- 1.需要安装Screen(或许可以使用你喜欢的保活方式)      
### Ubuntu使用

```bash  
sudo apt update
sudo apt-get install screen -y  
```

```bash
screen -S 你喜欢的名字
cd Jar的目录下
java -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 -jar Server.jar

# 退出使用Ctrl + A + D
#重进使用
screen -r 你设置的名字
#进不去使用 获取id
screen -ls
screen -r id
```
