#The MIT License (MIT)
#
#Copyright (c) 2017 Armel Soro
#
#Permission is hereby granted, free of charge, to any person obtaining a copy
#of this software and associated documentation files (the "Software"), to deal
#in the Software without restriction, including without limitation the rights
#to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#copies of the Software, and to permit persons to whom the Software is
#furnished to do so, subject to the following conditions:
#
#The above copyright notice and this permission notice shall be included in all
#copies or substantial portions of the Software.
#
#THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
#IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
#LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#SOFTWARE.

FROM adoptopenjdk:16-jdk-openj9 AS BUILD_IMAGE
LABEL maintainer="Armel Soro <armel@rm3l.org>"
ARG GRADLE_OPTS="-Dorg.gradle.daemon=false"
USER root
ENV APP_HOME=/code/service-names-port-numbers/
RUN mkdir -p $APP_HOME
WORKDIR $APP_HOME
COPY . .
RUN chmod 755 ./gradlew \
  && ./gradlew :service-names-port-numbers-app:build --info

FROM adoptopenjdk:16-jdk-openj9
LABEL maintainer="Armel Soro <armel@rm3l.org>"
ENV JAVA_OPTS=""
WORKDIR /root/
# Version in this Dockerfile should always be 0.1.0, because the .git folder is purposely not in the Docker build context.
# Therefore, the axion-release plugin is not able to determine the version based on the latest Git tag.
COPY --from=BUILD_IMAGE \
    /code/service-names-port-numbers/application/build/libs/service-names-port-numbers-app-0.1.0.jar \
    ./service-names-port-numbers-app.jar
EXPOSE 8080
EXPOSE 8081
RUN apt-get update && apt-get install --yes --force-yes \
	curl \
	netbase \
	&& rm -rf /var/lib/apt/lists/*
HEALTHCHECK --interval=5m --timeout=3s \
  CMD curl -f http://localhost:8081/management/health || exit 1
VOLUME /etc/rm3l
ENTRYPOINT exec \
    java \
    $JAVA_OPTS \
    -Djava.security.egd=file:/dev/./urandom \
    -jar /root/service-names-port-numbers-app.jar
