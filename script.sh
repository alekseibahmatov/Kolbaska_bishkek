#!/bin/bash
while true; do
    ./gradlew --stop
    ./gradlew -t build -x test &
    ./gradlew bootRun -Pdebug
done
