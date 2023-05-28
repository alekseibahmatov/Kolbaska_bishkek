FROM gradle:7.6.1-jdk17-jammy

RUN apt update && apt -y upgrade

RUN apt install -y inotify-tools tzdata

# Set the timezone.
ENV TZ=Europe/Tallinn
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8080 5005
