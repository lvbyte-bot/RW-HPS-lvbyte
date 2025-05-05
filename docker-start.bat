@echo off
echo 编译RW-HPS项目...
call gradlew jar

echo 构建Docker镜像...
docker-compose build

echo 启动RW-HPS容器...
docker-compose up -d

echo RW-HPS服务器已在Docker中启动！
echo 服务器端口: 5123
echo 数据目录: ./data
echo 配置目录: ./config
echo 插件目录: ./plugins
echo 地图目录: ./maps
echo 使用 'docker-compose logs -f' 查看服务器日志

pause
