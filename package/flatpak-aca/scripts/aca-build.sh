#!/bin/bash

# Strip SSL for db connection string
# TODO: Remove once tomcat is updated to match new ssl settings in connector
DB_URL="persistence.db.url"
NO_SSL_DB_URL="persistence.db.url                  = jdbc:mariadb://localhost/hirs_db?autoReconnect=true"

#BEFORE
cat HIRS_Utils/src/main/resources/persistence.properties

sed -i "\@${DB_URL}@c${NO_SSL_DB_URL}" HIRS_Utils/src/main/resources/persistence.properties

# CHECK
cat HIRS_Utils/src/main/resources/persistence.properties

PATH=/app/jre/bin:/usr/bin:/app/lib:/app/bin:/app/mariadb/bin
./gradlew :HIRS_AttestationCA:clean :HIRS_AttestationCA:build :HIRS_AttestationCAPortal:clean :HIRS_AttestationCAPortal:build
