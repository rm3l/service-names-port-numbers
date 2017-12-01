FROM openjdk:8-jdk-alpine AS BUILD_IMAGE
ENV APP_HOME=/root/dev/iana-service-names-port-numbers/
RUN mkdir -p $APP_HOME
WORKDIR $APP_HOME
COPY . .
RUN ./gradlew clean build

FROM openjdk:8-jre-alpine
ENV JAVA_OPTS=""
WORKDIR /root/
COPY --from=BUILD_IMAGE /root/dev/iana-service-names-port-numbers/application/build/libs/application-0.0.1-SNAPSHOT.jar ./iana-service-names-port-numbers.jar
EXPOSE 8080
EXPOSE 8081
RUN apk add --no-cache curl
HEALTHCHECK --interval=5m --timeout=3s \
  CMD curl -f http://localhost:8081/management/health || exit 1
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /root/iana-service-names-port-numbers.jar
