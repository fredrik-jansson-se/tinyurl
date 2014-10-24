tinyurl
=======

Setup
=====
Run setup.sh
This will download a build redis

Configuration
=============
Edit frontend/src/main/resources/application.conf

finagle_address - Bind the web server to this address, e.g. localhost:8080
public_address - Address sent to the clients, e.g. http://localhost:8080 => http://localhost:8080/lookup/abcdef

hash_size - number of characters in the generated tiny URL

redis_master - host:port to the Redis master, writes will happen here - example localhost:6379
redis_slave - semicolon separated list of slaves, lookups will go to these - example localhost:7777;localhost:8888

Run
===
If tmux is installed, please run start.sh

If not
In one window run redis/src/redis-server
In another once run sbt frontend/run

Test the solution with e.g. 
curl -d "URL=http://www.google.se" http://localhost:8080/create

curl -v http://localhost:8080/lookup/foobar 

Solution returns HTTP 302 if lookup is successful, 404 if not


Possible Improvements
=====================
Check for hash collisions, if that happens, use a one char longer hash

If the master redis server failes, promote a slave to master
If a slave failes, mark it as bad, maybe just for a while.
Slaves are picked randomly, possible to configure for e.g. round robin



