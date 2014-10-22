#!/bin/sh

if [ ! -d redis ]; then
  git clone -b 2.8 https://github.com/antirez/redis.git
  cd redis
  make
  cd ..
fi
