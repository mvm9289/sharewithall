#!/bin/bash

kill -9 `jps | grep SWAServerRMI | cut -d ' ' -f 1`
killall rmiregistry
