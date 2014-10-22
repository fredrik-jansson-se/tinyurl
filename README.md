tinyurl
=======

Setup
=====
Run setup.sh
This will download a build redis


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
Check for hash collisions, if that happens, keep a one char longer hash

Keep a list of slave redis servers, round robin look ups.
If the master redis server failes, promote a slave to master



