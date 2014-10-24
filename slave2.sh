!/bin/sh

redis/src/./redis-server --port 8888 --slaveof 127.0.0.1 6379
