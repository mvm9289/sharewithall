#!/bin/bash

cp ../resources/connection.properties ../bin/
cd ../bin
rmiregistry 4040 &
java ShareWithAll.Server.RMI.SWAServerRMI &
