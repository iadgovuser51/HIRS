#!/bin/bash

PATH=/app/jre/bin:/usr/bin:/app/lib:/app/bin:/app/mariadb/bin
PROTOC_HOME=/app/bin
JAVA_HOME=/app/jre
./gradlew :HIRS_AttestationCA:clean :HIRS_AttestationCA:build :HIRS_AttestationCAPortal:clean :HIRS_AttestationCAPortal:build
