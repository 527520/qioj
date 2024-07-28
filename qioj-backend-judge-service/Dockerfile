# 基础镜像
FROM openjdk:8-jdk-alpine
# 指定工作目录
WORKDIR /app
# 将jar包添加到工作目录
ADD target/qioj-backend-judge-service-0.0.1-SNAPSHOT.jar .
# 设置 JVM 内存参数
ENV JAVA_OPTS="-Xms128m -Xmx256m"
# 暴露端口号
EXPOSE 8114
# 启动命令
ENTRYPOINT ["java","$JAVA_OPTS","-jar","/app/qioj-backend-judge-service-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]