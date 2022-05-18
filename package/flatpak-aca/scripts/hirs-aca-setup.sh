#!/bin/bash

#install tomcat
/app/bin/tomcat-install.sh

#initialize mariadb
/app/bin/mariadb-install.sh

HIRS_DIR=/var/data/hirs
cp /app/hirs -R /var/data

# Copy war files to tomcat
cp ${HIRS_DIR}/wars/HIRS_AttestationCA.war /var/tomcat/webapps
cp ${HIRS_DIR}/wars/HIRS_AttestationCAPortal.war /var/tomcat/webapps

# Let wars unpack
/var/tomcat/bin/catalina.sh start
sleep 5
/var/tomcat/bin/catalina.sh stop
chmod -R 777 /var/tomcat/webapps
# # Write permissions for accesscd
cd /var/tomcat/
chmod -R 777 webapps temp logs work conf

# # Setup certificates
# # create trust stores, configure tomcat and db
echo "Starting ssl_configure"

# # TODO: Do not start mariadb until my.cnf is loaded for SSL enable
sh ${HIRS_DIR}/scripts/ssl_configure.sh server

#Start mariadb
mysqld --no-defaults --console --basedir=/var/mariadb/ --datadir=/var/mariadb/data/ &

echo "Starting Mariadb"
sleep 5

# # create the database
echo "Creating hirs database"
sh ${HIRS_DIR}/scripts/db_create.sh

# Create certs
echo "Starting cert generation"
sh ${HIRS_DIR}/scripts/certificate_generate.sh

# Start Tomcat
/var/tomcat/bin/catalina.sh start