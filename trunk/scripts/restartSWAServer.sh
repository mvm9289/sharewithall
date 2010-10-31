#!/bin/bash

kill -9 `jps | grep SWAServerRMI | cut -d ' ' -f 1`
killall rmiregistry
cp ../resources/connection.properties ../bin/
cd ../bin
rmiregistry 4040 &
java ShareWithAll.Server.RMI.SWAServerRMI &
