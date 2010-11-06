#!/bin/bash

kill -9 `jps | grep SWAServer | cut -d ' ' -f 1`
sleep 1
cp /home/sharewithall/SWAServer/resources/connection.properties /home/sharewithall/SWAServer/bin/
cd /home/sharewithall/SWAServer/bin
java sharewithall.server.SWAServer &
