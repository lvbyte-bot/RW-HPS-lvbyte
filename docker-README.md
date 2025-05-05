# RW-HPS Docker 使用指南

本文档介绍如何使用Docker运行RW-HPS服务器，无需每次都配置环境。

## 前提条件

- 安装 [Docker](https://www.docker.com/get-started)
- 安装 [Docker Compose](https://docs.docker.com/compose/install/)

## 快速开始

### Windows用户

1. 双击运行 `docker-start.bat` 脚本
2. 等待编译、构建和启动完成
3. 服务器将在Docker容器中运行，端口为5123

### Linux/Mac用户

1. 给脚本添加执行权限：`chmod +x docker-start.sh`
2. 运行脚本：`./docker-start.sh`
3. 等待编译、构建和启动完成
4. 服务器将在Docker容器中运行，端口为5123

## 目录说明

Docker容器会自动创建以下目录，用于数据持久化：

- `./data`：服务器数据目录
- `./config`：配置文件目录
- `./plugins`：插件目录
- `./maps`：地图目录

## 常用命令

- 查看服务器日志：`docker-compose logs -f`
- 停止服务器：`docker-compose down`
- 重启服务器：`docker-compose restart`
- 更新服务器（重新编译后）：`docker-compose up -d --build`

## 自定义配置

如需修改服务器配置，可以编辑 `docker-compose.yml` 文件中的环境变量：

```yaml
environment:
  - TZ=Asia/Shanghai  # 时区设置
  - JAVA_OPTS=-Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 -Xmx1G  # Java选项
```

## 端口说明

- TCP 5123：游戏服务器主端口
- UDP 5000-6000：游戏通信端口范围

如需修改端口映射，请编辑 `docker-compose.yml` 文件中的 `ports` 部分。
