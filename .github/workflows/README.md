# GitHub Actions 自动构建与Docker发布

本文档介绍如何使用GitHub Actions自动构建RW-HPS项目并发布Docker镜像。

## 功能

- 自动编译项目
- 构建Docker镜像
- 发布到GitHub Container Registry (GHCR)
- 上传构建产物作为Artifact

## 触发方式

工作流会在以下情况下触发：

1. 推送代码到`master`或`main`分支
2. 创建新的标签（以`v`开头，如`v1.0.0`）
3. 创建Pull Request到`master`或`main`分支
4. 手动触发（通过GitHub Actions界面）

## Docker镜像标签

Docker镜像会使用以下标签：

- 对于标签发布：`v1.0.0`、`1.0`和`latest`
- 对于分支：分支名称（如`master`、`main`）
- 对于Pull Request：PR编号
- 对于所有构建：短SHA值（提交哈希的前7位）

## 使用Docker镜像

从GitHub Container Registry拉取镜像：

```bash
docker pull ghcr.io/[用户名]/rw-hps:[标签]
```

运行容器：

```bash
docker run -d \
  --name rw-hps \
  -p 5123:5123 \
  -p 5000-6000:5000-6000/udp \
  -v ./data:/app/data \
  -v ./config:/app/config \
  -v ./plugins:/app/plugins \
  -v ./maps:/app/maps \
  ghcr.io/[用户名]/rw-hps:[标签]
```

## 使用docker-compose

创建`docker-compose.yml`文件：

```yaml
version: '3'

services:
  rw-hps:
    image: ghcr.io/[用户名]/rw-hps:[标签]
    container_name: rw-hps
    ports:
      - "5123:5123"
      - "5000-6000:5000-6000/udp"
    volumes:
      - ./data:/app/data
      - ./config:/app/config
      - ./plugins:/app/plugins
      - ./maps:/app/maps
    restart: unless-stopped
    environment:
      - TZ=Asia/Shanghai
      - JAVA_OPTS=-Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 -Xmx1G
```

然后运行：

```bash
docker-compose up -d
```

## 注意事项

- 确保仓库设置中启用了GitHub Actions
- 确保仓库设置中允许GitHub Actions创建和推送包
- 如果是私有仓库，需要设置适当的权限以允许其他人拉取镜像
