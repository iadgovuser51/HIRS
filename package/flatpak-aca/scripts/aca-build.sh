#!/bin/bash

ACA_DIR=/var/tmp/hirs/aca

# Strip SSL for db connection string
# TODO: Remove once tomcat is updated to match new ssl settings in connector
DB_URL="persistence.db.url"
NO_SSL_DB_URL="persistence.db.url                  = jdbc:mariadb://localhost/hirs_db?autoReconnect=true"
sed -i "\@${DB_URL}@c${NO_SSL_DB_URL}" HIRS_Utils/src/main/resources/persistence.properties
sed -i "\@${DB_URL}@c${NO_SSL_DB_URL}" /app/hirs/config/default-properties/attestationca/persistence.properties

# modify aca directory and password
ACA_ROOT="aca.directories.root              = /etc/hirs/aca"
NEW_ACA_ROOT="aca.directories.root              = ${ACA_DIR}"
sed -i "\@${ACA_ROOT}@c${NEW_ACA_ROOT}" HIRS_AttestationCA/src/main/resources/defaults.properties
sed -i "s/aca\.keyStore\.password\s*=/aca.keyStore.password=password/" HIRS_AttestationCA/src/main/resources/defaults.properties

# Build wars
PATH=/app/jre/bin:/usr/bin:/app/lib:/app/bin
./gradlew :HIRS_AttestationCA:clean :HIRS_AttestationCA:build :HIRS_AttestationCAPortal:clean :HIRS_AttestationCAPortal:build
