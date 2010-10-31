#!/bin/bash

cp /home/sharewithall/SWAServer/resources/connection.properties /home/sharewithall/SWAServer/bin/
cd /home/sharewithall/SWAServer/bin
rmiregistry 4040 &
java ShareWithAll.Server.RMI.SWAServerRMI &
