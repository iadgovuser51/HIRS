#!/bin/bash

# Create structure in /var
echo "Creating mariadb layout in /var"
mkdir /var/mariadb
mkdir /var/mariadb/data

# Initialize data base
echo "Initializing database tables"
cd /app/mariadb

# Start mariadb
mysqld --no-defaults --console --basedir=/var/mariadb/ --datadir=/var/mariadb/data/ &

# Sleep 5 seconds for database setup
echo "Sleeping for 5 seconds for db setup"
sleep 4

# kill mysqld for system tables creation
echo "Killing mariadb"
kill $(ps aux | grep '[m]ysqld' | awk '{print $2}')

# Create system tables
echo "Creating system tables"
scripts/mysql_install_db --datadir=/var/mariadb/data &
