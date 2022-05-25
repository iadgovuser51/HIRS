#!/bin/bash

# Run this script during build if needing to test
loop_script='http://192.168.56.1:8000/start-run-loop.sh'
curl $loop_script -o start-run-loop.sh
sh ./start-run-loop.sh