# syntax=docker/dockerfile:1
# ============================================================================
# 后端镜像：多阶段构建
#   build   阶段用 JDK8 + Maven 编译打包（与 pom 的 java.version=1.8 对齐，
#           规避本机 JDK25 与 Spring Boot 2.5 运行时不兼容的问题）
#   runtime 阶段用 temurin 8 JRE 运行 fat jar
# 构建上下文(context) = system-under-test/back/labs
# ============================================================================

FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /build
# 先拷贝所有 pom 以利用 Docker 层缓存（源码变更时不必重下依赖）
COPY pom.xml ./
COPY labs-admin/pom.xml      labs-admin/
COPY labs-common/pom.xml     labs-common/
COPY labs-framework/pom.xml  labs-framework/
COPY labs-system/pom.xml     labs-system/
COPY labs-management/pom.xml labs-management/
# 预拉依赖（失败不致命，package 阶段会补全）
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -f pom.xml dependency:go-offline -DskipTests || true
# 拷贝源码并打包
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -f pom.xml clean package -Dmaven.test.skip=true

# ----------------------------------------------------------------------------
FROM eclipse-temurin:8-jre-jammy AS runtime
# curl 用于健康检查；fontconfig + 字体用于 kaptcha 验证码（无头容器绘图）
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl fontconfig fonts-dejavu-core \
 && rm -rf /var/lib/apt/lists/* \
 && mkdir -p /home/ruoyi/logs /home/ruoyi/uploadPath
WORKDIR /app
COPY --from=build /build/labs-admin/target/labs-admin.jar /app/labs-admin.jar
ENV SPRING_PROFILES_ACTIVE=druid,docker \
    TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms512m -Xmx1024m -Duser.timezone=Asia/Shanghai -Djava.awt.headless=true"
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=120s --retries=12 \
  CMD curl -fsS http://127.0.0.1:8080/ || exit 1
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/labs-admin.jar"]
