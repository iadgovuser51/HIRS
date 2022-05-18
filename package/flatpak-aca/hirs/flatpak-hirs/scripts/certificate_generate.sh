#!/usr/bin/env bash

# hirs directory
HIRS_DIR=/var/data/hirs

# variables for the CA certificates
CA_PATH=${HIRS_DIR}/certificates
CA_KEYSTORE=${CA_PATH}/TrustStore.jks

# variables for the ACA certificates
ACA_CERTS=${HIRS_DIR}/certificates
ACA_KEY=${ACA_CERTS}/aca.key
ACA_CRT=${ACA_CERTS}/aca.crt
ACA_P12=${ACA_CERTS}/aca.p12
ACA_JKS=${ACA_CERTS}/keyStore.jks
ACA_CONF=${ACA_CERTS}/aca.conf

touch ${ACA_CONF}

# generate the OpenSSL conf file
echo "[req]" >> ${ACA_CONF}
echo "req_extensions=aca" >> ${ACA_CONF}
echo "distinguished_name=distname" >> ${ACA_CONF}
echo "" >> ${ACA_CONF}
echo "[aca]" >> ${ACA_CONF}
echo "keyUsage=critical,keyCertSign" >> ${ACA_CONF}
echo "basicConstraints=critical,CA:true" >> ${ACA_CONF}
echo "subjectKeyIdentifier=hash" >> ${ACA_CONF}
echo "" >> ${ACA_CONF}
echo "[distname]" >> ${ACA_CONF}
echo "# empty" >> ${ACA_CONF}

# generate the ACA signing key and self-signed certificate
openssl req -x509 -config ${ACA_CONF} -extensions aca -days 3652 -set_serial 01 -subj "/C=US/O=HIRS/OU=Attestation CA/CN=$HOSTNAME" -newkey rsa:2048 -nodes -keyout ${ACA_KEY} -out ${ACA_CRT}

# if the trust store already has an older HIRS_ACA_KEY in it, remove it
keytool -list -keystore ${CA_KEYSTORE} -storepass password -alias HIRS_ACA_KEY
rc=$?
if [[ $rc = 0 ]]; then
	keytool -delete -alias HIRS_ACA_KEY -storepass password -keystore ${CA_KEYSTORE}
fi

# load the generated certificate into the CA trust store
keytool -import -keystore ${CA_KEYSTORE} -storepass password -file ${ACA_CRT} -noprompt -alias HIRS_ACA_KEY

# export the cert and key to a p12 file
openssl pkcs12 -export -in ${ACA_CRT} -inkey ${ACA_KEY} -out ${ACA_P12} -passout pass:password

# create a key store using the p12 file
keytool -importkeystore -srckeystore ${ACA_P12} -destkeystore ${ACA_JKS} -srcstoretype pkcs12 -srcstorepass password -deststoretype jks -deststorepass password -noprompt -alias 1 -destalias HIRS_ACA_KEY

# set the password in the aca properties file
sed -i "s/aca\.keyStore\.password\s*=/aca.keyStore.password=password/" ${HIRS_DIR}/config/aca.properties

# copy the trust store to the ACA
mkdir ${HIRS_DIR}/certificates/client-files/
cp ${CA_KEYSTORE} ${HIRS_DIR}/certificates/client-files/
