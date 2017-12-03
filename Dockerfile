FROM gradle:4.3.1-jdk8-alpine AS BUILD_IMAGE
ARG GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.parallel=false"
ARG GRADLE_OPTS="$GRADLE_OPTS -Dkotlin.incremental=false -Dkotlin.compiler.execution.strategy=in-process"
USER root
ENV APP_HOME=/code/iana-service-names-port-numbers/
RUN mkdir -p $APP_HOME
WORKDIR $APP_HOME
COPY . .
RUN chown -R gradle:gradle $APP_HOME
USER gradle
RUN gradle clean build --stacktrace

FROM openjdk:8-jre-alpine
ENV JAVA_OPTS=""
WORKDIR /root/
COPY --from=BUILD_IMAGE \
    /code/iana-service-names-port-numbers/application/build/libs/iana-service-names-port-numbers-app-0.1.3.jar \
    ./iana-service-names-port-numbers-app.jar
EXPOSE 8080
EXPOSE 8081
RUN apk add --no-cache curl
HEALTHCHECK --interval=5m --timeout=3s \
  CMD curl -f http://localhost:8081/management/health || exit 1
ENTRYPOINT exec \
    java \
    $JAVA_OPTS \
    -Djava.security.egd=file:/dev/./urandom \
    -jar /root/iana-service-names-port-numbers-app.jar
