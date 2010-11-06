#!/bin/bash

kill -9 `jps | grep SWAServer | cut -d ' ' -f 1`
