FROM openjdk:17-alpine

ENV GRADLE_VERSION 7.6

RUN set -o errexit -o nounset \
    && echo "Downloading Gradle" \
    && wget --no-verbose --output-document=gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
    \
    && echo "Installing Gradle" \
    && unzip gradle.zip \
    && rm gradle.zip \
    && mv "gradle-${GRADLE_VERSION}" "/usr/lib/gradle" \
    && ln -s "/usr/lib/gradle/bin/gradle" /usr/bin/gradle

COPY . /app
WORKDIR /app

RUN mkdir /var/data
RUN mkdir /var/data/photos
RUN mkdir /var/data/contracts

RUN gradle clean build -x test -Pproduction

EXPOSE 8080
CMD ["java", "-jar", "build/libs/Kolbaska-0.0.1-SNAPSHOT.jar"]