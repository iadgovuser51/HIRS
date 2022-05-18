#!/bin/bash

# Strip SSL for db connection string
# TODO: Remove once tomcat is updated to match new ssl settings in connector
DB_URL="persistence.db.url"
NO_SSL_DB_URL="persistence.db.url                  = jdbc:mariadb://localhost/hirs_db?autoReconnect=true"
sed -i "\@${DB_URL}@c${NO_SSL_DB_URL}" HIRS_Utils/src/main/resources/persistence.properties

# modify aca directory
ACA_ROOT="aca.directories.root              = /etc/hirs/aca"
NEW_ACA_ROOT="aca.directories.root              = /var/data/hirs"
sed -i "\@${ACA_ROOT}@c${NEW_ACA_ROOT}" HIRS_AttestationCA/src/main/resources/defaults.properties

# Build wars
PATH=/app/jre/bin:/usr/bin:/app/lib:/app/bin:/app/mariadb/bin
./gradlew :HIRS_AttestationCA:clean :HIRS_AttestationCA:build :HIRS_AttestationCAPortal:clean :HIRS_AttestationCAPortal:build
mkdir /app/hirs/wars