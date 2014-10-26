tinyurl
=======

Prerequisites
=============
make, c-compiler, java

Setup and Run
=============
./setup.sh

This will download and build redis

./start.sh

This will start: 

* 1 Redis master
* 2 Redis slaves
* 1 HTTP Frontend

Test
====
Create a tiny url:
* curl -d "URL=http://www.google.se" http://localhost:8080/create

Test a tiny url
* curl -v http://localhost:8080/lookup/7c7fea

Solution returns HTTP 302 if lookup is successful, 404 if not

Configuration
=============
Edit frontend/src/main/resources/application.conf

* finagle_address - Bind the web server to this address, e.g. localhost:8080
* public_address - Address sent to the clients, e.g. http://localhost:8080 => http://localhost:8080/lookup/abcdef

* hash_size - number of characters in the generated tiny URL

* redis_master - host:port to the Redis master, writes will happen here - example localhost:6379
* redis_slave - semicolon separated list of slaves, lookups will go to these - example localhost:7777;localhost:8888

Possible Improvements
=====================
* Check for hash collisions, if that happens, use a one char longer hash
* If the master redis server failes, promote a slave to master
* If a slave failes, mark it as bad, maybe just for a while.


