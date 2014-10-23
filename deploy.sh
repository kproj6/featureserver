#!/bin/sh
# This script will ssh into the server, fetch the latest commits from github, build the program, stop the old process and start a new one.

SERVER_IP=178.62.233.73
ssh featureserver@$SERVER_IP '
    cd ~/featureserver
    git fetch
    git reset --hard origin/master
    mvn clean package
    ./svc.sh stop
    ./svc.sh start
'
