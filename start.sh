#!/bin/sh

#Start redis server
redis/src/redis-server &
SRV=$!

#start slaves
redis/src/./redis-server --port 7777 --slaveof 127.0.0.1 6379 &
SLAVE_1=$!
redis/src/./redis-server --port 8888 --slaveof 127.0.0.1 6379 &
SLAVE_2=$!

./sbt frontend/run

kill $SRV $SLAVE_1 $SLAVE_2
