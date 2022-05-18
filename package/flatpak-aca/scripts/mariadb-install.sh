#!/bin/bash

# Create structure in /var
echo "Creating mariadb layout in /var"
mkdir /var/mariadb
mkdir /var/mariadb/data

# Initialize data base
echo "Initializing database tables"
cd /app/mariadb

# # Sleep 5 seconds for database setup
# echo "Sleeping for 5 seconds"
# sleep 5

# # kill mysqld for system tables creation
# echo "Killing mariadb"
# kill $(ps aux | grep '[m]ysqld' | awk '{print $2}')

# # Create system tables
# scripts/mysql_install_db --datadir=/var/mariadb/data &

# Start mariadb again
mysqld --no-defaults --console --skip-grant-tables  --basedir=/var/mariadb/ --datadir=/var/mariadb/data/ &
