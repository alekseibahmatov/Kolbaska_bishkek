#FROM openjdk:17-alpine
#
#ENV GRADLE_VERSION 7.6
#
#RUN set -o errexit -o nounset \
#    && echo "Downloading Gradle" \
#    && wget --no-verbose --output-document=gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
#    \
#    && echo "Installing Gradle" \
#    && unzip gradle.zip \
#    && rm gradle.zip \
#    && mv "gradle-${GRADLE_VERSION}" "/usr/lib/gradle" \
#    && ln -s "/usr/lib/gradle/bin/gradle" /usr/bin/gradle
#
#COPY . /app
#WORKDIR /app
#
#RUN mkdir /var/data
#RUN mkdir /var/data/photos
#RUN mkdir /var/data/contracts
#
#RUN gradle clean build -x test -Pproduction
#
#EXPOSE 8080
#CMD ["java", "-jar", "build/libs/Kolbaska-0.0.1-SNAPSHOT.jar"]

# Stage 1: Build the application
FROM gradle:7.6.0-jdk17 as build

# Set the working directory
WORKDIR /home/gradle/project

# Copy the gradle files
COPY build.gradle settings.gradle ./

# Copy the source code
COPY src ./src

# Run the build
RUN gradle clean build

# Stage 2: Run the application
FROM openjdk:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# Run the application
CMD ["java", "-jar", "app.jar"]
