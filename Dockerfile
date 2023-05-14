FROM gradle:7.6.1-jdk17-jammy

RUN apt update && apt -y upgrade

RUN apt install -y inotify-tools

EXPOSE 8080 5005