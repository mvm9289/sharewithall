#!/bin/bash

kill -9 `jps | grep SWAServerRMI | cut -d ' ' -f 1`
killall rmiregistry
sleep 1
cp /home/sharewithall/SWAServer/resources/connection.properties /home/sharewithall/SWAServer/bin/
cd /home/sharewithall/SWAServer/bin
rmiregistry 4040 &
java -Djava.rmi.server.hostname=192.168.1.122 ShareWithAll.Server.RMI.SWAServerRMI &
