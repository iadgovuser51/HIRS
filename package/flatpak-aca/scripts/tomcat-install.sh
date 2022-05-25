#!/bin/bash

TOMCAT_DIR=/var/tmp/tomcat

# If tomcat not in non-volatile storage
if [ ! -d "${TOMCAT_DIR}" ] 
then
	mkdir $TOMCAT_DIR
	cp /app/tomcat/. -R $TOMCAT_DIR
	cd $TOMCAT_DIR
	chmod -R 777 webapps temp logs work conf
	echo "Tomcat copied to non-volatile storage"

	# Unpack ACA WAR files
	echo "Unpacking ACA War files"
	$TOMCAT_DIR/bin/catalina.sh start
	echo "Sleeping for 8 seconds"
	sleep 8
	echo "Stoping tomcat"
	$TOMCAT_DIR/bin/catalina.sh stop
	echo "Writing permissions to webapps"
	chmod -R 777 $TOMCAT_DIR/webapps
	echo "Finished unpacking ACA War Files"
else
	echo "Tomcat installation found"
fi
