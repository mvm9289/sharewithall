#!/bin/bash

cd /home/sharewithall/SWAServer
cp bin/ShareWithAll/Server/RMI/* sources/ShareWithAll/Server/RMI/
cd sources
rmic ShareWithAll.Server.RMI.SWAServerRMIImplementation
cd ..
mv sources/ShareWithAll/Server/RMI/SWAServerRMIImplementation_Stub.class bin/ShareWithAll/Server/RMI/
rm sources/ShareWithAll/Server/RMI/*.class

echo "SWAServerRMIImplementation local stub done!"
