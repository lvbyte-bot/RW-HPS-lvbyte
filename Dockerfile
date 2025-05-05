FROM azul/zulu-openjdk:21-jre

LABEL maintainer="RW-HPS Docker"
LABEL description="Docker image for RW-HPS (Rusted Warfare High Performance Server)"

# 设置工作目录
WORKDIR /app

# 复制编译好的JAR文件
COPY Server-All/build/libs/Server-All.jar /app/Server-All.jar

# 创建数据目录
RUN mkdir -p /app/data /app/config /app/plugins /app/maps

# 暴露默认端口
EXPOSE 5123
EXPOSE 5000-6000/udp

# 设置卷，方便数据持久化
VOLUME ["/app/data", "/app/config", "/app/plugins", "/app/maps"]

# 设置环境变量
ENV JAVA_OPTS="-Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8"

# 启动命令
CMD ["sh", "-c", "java $JAVA_OPTS -jar Server-All.jar"]
