#!/bin/bash

cp /app/tomcat -R /var
cd /var/tomcat
chmod -R 777 webapps temp logs work conf
echo "Tomcat ready for start"
