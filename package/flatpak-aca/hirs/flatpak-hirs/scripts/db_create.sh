#!/bin/bash

HIRS_DIR=/var/tmp/hirs

DB_CREATE_SCRIPT=$HIRS_DIR/scripts/db_create.sql
mysql -u root < $DB_CREATE_SCRIPT