#!/bin/bash

cp /home/sharewithall/SWAServer/resources/connection.properties /home/sharewithall/SWAServer/bin/
cd /home/sharewithall/SWAServer/bin
java sharewithall.server.SWAServer &
