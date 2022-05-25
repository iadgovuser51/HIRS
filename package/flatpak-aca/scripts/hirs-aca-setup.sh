#!/bin/bash

# install tomcat
echo "Running tomcat script"
( source /app/bin/tomcat-install.sh; wait ) & wait


# initialize mariadb
echo "Running mariadb script"
( source /app/bin/mariadb-install.sh; wait ) & wait

HIRS_DIR=/var/tmp/hirs
TOMCAT_DIR=/var/tmp/tomcat
MARIADB_DIR=/var/tmp/mariadb

# Setup HIRS directories
if [ ! -d "${HIRS_DIR}" ] 
then
	echo "No HIRS directory found"
	mkdir $HIRS_DIR
	cp /app/hirs/. -R $HIRS_DIR/
	# # Setup certificates
	# # create trust stores, configure tomcat and db
	echo "Starting ssl_configure"

	# # TODO: Do not start mariadb until my.cnf is loaded for SSL enable
	( source ${HIRS_DIR}/scripts/ssl_configure.sh server; wait ) & wait

	#Start mariadb
	mysqld --no-defaults --console --basedir=$MARIADB_DIR --datadir=$MARIADB_DIR/data &

	echo "Starting mariadb"
	sleep 5

	# # create the database
	echo "Creating hirs database"
	sh ${HIRS_DIR}/scripts/db_create.sh

	# Create certs
	echo "Starting cert generation"
	# ( source ${HIRS_DIR}/scripts/certificate_generate.sh; wait ) & wait
	sh ${HIRS_DIR}/scripts/certificate_generate.sh
	sleep 5

	# Start Tomcat
	echo "Starting tomcat"
	$TOMCAT_DIR/bin/catalina.sh start
else
	echo "HIRS Installation found"
	#start mariadb
	echo "Starting mariadb"
	mysqld --no-defaults --console --basedir=$MARIADB_DIR --datadir=$MARIADB_DIR/data &
	sleep 3
	# Start Tomcat
	echo "Starting tomcat"
	$TOMCAT_DIR/bin/catalina.sh start
fi