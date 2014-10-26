tinyurl
=======

Setup and Run
=============
Run setup.sh
This will download a build redis

Run start.sh
This will start: 
1 Redis master
2 Redis slaves
1 HTTP Frontend

Test
====
Test the solution with e.g. 
curl -d "URL=http://www.google.se" http://localhost:8080/create

curl -v http://localhost:8080/lookup/foobar 

Solution returns HTTP 302 if lookup is successful, 404 if not

Configuration
=============
Edit frontend/src/main/resources/application.conf

finagle_address - Bind the web server to this address, e.g. localhost:8080
public_address - Address sent to the clients, e.g. http://localhost:8080 => http://localhost:8080/lookup/abcdef

hash_size - number of characters in the generated tiny URL

redis_master - host:port to the Redis master, writes will happen here - example localhost:6379
redis_slave - semicolon separated list of slaves, lookups will go to these - example localhost:7777;localhost:8888

Possible Improvements
=====================
Check for hash collisions, if that happens, use a one char longer hash

If the master redis server failes, promote a slave to master
If a slave failes, mark it as bad, maybe just for a while.


