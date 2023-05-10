#!/bin/bash
./gradlew --stop
./gradlew -t build -x test &
./gradlew bootRun -Pdebug