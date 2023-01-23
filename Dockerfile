FROM openjdk:17-jdk-slim-buster as build
COPY . /app
WORKDIR /app
RUN ./gradlew clean build

FROM openjdk:17-jdk-slim-buster
COPY --from=build /app/build/libs/Kolbaska-0.0.1-SNAPSHOT.jar /app/spring-boot-app.jar
EXPOSE "8080"
CMD java -jar /app/spring-boot-app.jar