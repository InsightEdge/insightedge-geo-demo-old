#!/usr/bin/env bash

grepKill() {
    local title=$1
    local search=$2

    pid=`ps aux | grep -v grep | grep $2 | awk '{print $2}'`
    if [ -z $pid ]; then
        echo "$title is not running"
        return
    fi
    echo "Stopping $title (pid: $pid)..."

    kill -9 $pid

    TIMEOUT=60
    while ps -p $pid > /dev/null; do
	if [ $TIMEOUT -le 0 ]; then
	    break
	fi
    	echo "  waiting termination ($TIMEOUT sec)"
    	((TIMEOUT--))
    	sleep 1
    done
    echo "$title stopped"
}

grepKill ZooKeeper config/zookeeper.properties
grepKill Kafka config/server.properties