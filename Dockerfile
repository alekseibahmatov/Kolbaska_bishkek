FROM debian:stable-slim

RUN apt update && apt upgrade

RUN apt install -y openjdk-17-jdk

EXPOSE 8080 5005