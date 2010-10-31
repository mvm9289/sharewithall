#!/bin/bash

kill -9 `jps | grep SWAServerRMI | cut -d ' ' -f 1`
killall rmiregistry
sleep 1
cp /home/sharewithall/SWAServer/resources/connection.properties /home/sharewithall/SWAServer/bin/
cd /home/sharewithall/SWAServer/bin
rmiregistry 4040 &
java ShareWithAll.Server.RMI.SWAServerRMI &
