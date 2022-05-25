#!/bin/bash

MARIADB_DIR=/var/tmp/mariadb

# Check if a mariadb data does not exist
if [ ! -d "${MARIADB_DIR}/data" ] 
then
	# Create mariadb folder structure in /var/tmp
	echo "Creating mariadb layout in /var/tmp"
	mkdir $MARIADB_DIR
	mkdir $MARIADB_DIR/data

	cp /app/mariadb/. -R $MARIADB_DIR/
	chmod -R 755 $MARIADB_DIR

	# Initialize data base
	echo "Initializing database tables"
	mysqld --no-defaults --console --basedir=$MARIADB_DIR --datadir=$MARIADB_DIR/data/ &

	# Sleep 5 seconds for database setup
	echo "Sleeping for 5 seconds for db setup"
	sleep 5

	# kill mysqld for system tables creation
	echo "Killing mariadb"
	kill $(ps aux | grep '[m]ysqld' | awk '{print $2}')
	sleep 3

	# Create system tables
	echo "Creating system tables"
	cd $MARIADB_DIR
	scripts/mysql_install_db --datadir=$MARIADB_DIR/data
	sleep 5

fi

