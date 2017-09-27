#!/usr/bin/env bash

SCRIPTS_DIR=$(dirname "$0")

pushd $SCRIPTS_DIR/..

echo "Starting feeder"
nohup java -jar target/feeder.jar > ~/feeder.log &

# restart every 1 hour
while sleep 3600; do
    echo "Restarting feeder"
    date
    pkill -f target/feeder
    nohup java -jar target/feeder.jar > ~/feeder.log &
done

popd