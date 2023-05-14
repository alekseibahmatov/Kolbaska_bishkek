#!/bin/bash

# Start the gradle bootRun process in the background
gradle bootRun -Pdebug &

# Remember its PID
pid=$!

while true; do
  inotifywait -r -e modify,create,delete src

  # Kill the old gradle bootRun process
  kill $pid

  # Start a new gradle bootRun process in the background
  gradle bootRun -Pdebug &

  # Remember its PID
  pid=$!
done
