# Docker 镜像构建
# @author shinkte
# @from shinkte
#Dockerfile包含了一系列指令来定义容器的过程，每条指令构建一层，在构建镜像的过程中，会一层一层地叠加，最终生成一个完整的镜像。

#指定基础镜像，使用的是 maven:3.5-jdk-8-alpine，这是一个包含 Maven 和 JDK 8 的轻量级 Alpine Linux 镜像
FROM maven:3.5-jdk-8-alpine as builder

# Copy local code to the container image.
#设置容器内的工作目录
WORKDIR /app
#复制pom.xml和源代码到容器内的工作目录
COPY pom.xml .
#复制源代码到容器内的工作目录
COPY src ./src

# 用于构建过程中执行命令，这里是编译源代码并打包成jar包
RUN mvn package -DskipTests

# 设置容器启动时执行的命令
CMD ["java","-jar","/app/target/MatchSystem-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]