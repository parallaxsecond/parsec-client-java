#!/bin/bash -e
# script runs on nginx after ports and hosts are known

HOST_PORT="$1"
PASSWORD="$2"
ROOT="${3:-.}"

mkdir -p "${ROOT}"/keys
cd "${ROOT}"/keys

mkdir -p "${ROOT}"/etc/nginx/conf.d
cat >"${ROOT}"/etc/nginx/conf.d/ssl.conf << 'EOF'
server {
  listen 443 ssl;
  server_name example.com;
  ssl_protocols TLSv1.1 TLSv1.2;
  ssl_certificate /keys/server_chain.crt;
  ssl_certificate_key /keys/server.key;
  # allow all clients signed by ca
  ssl_client_certificate /keys/ca.crt;
  ssl_verify_client optional;

  location / {
    if ($ssl_client_verify != SUCCESS) {
      return 403;
    }
    return 200;
  }
}
EOF

cat >ca.cnf << 'EOF'
FQDN = ca_cert
ORGNAME = ParsecTest
ALTNAMES = DNS:$FQDN   # , DNS:bar.example.org , DNS:www.foo.example.org
# --- no modifications required below ---
[ req ]
default_bits = 2048
default_md = sha256
prompt = no
encrypt_key = no
distinguished_name = dn
req_extensions = req_ext
[ dn ]
C = CH
O = $ORGNAME
CN = $FQDN
[ req_ext ]
subjectAltName = $ALTNAMES
EOF

cat >client.cnf << 'EOF'
FQDN = client_cert
ORGNAME = ParsecTest
ALTNAMES = DNS:$FQDN   # , DNS:bar.example.org , DNS:www.foo.example.org

# --- no modifications required below ---
[ req ]
default_bits = 2048
default_md = sha256
prompt = no
encrypt_key = no
distinguished_name = dn
req_extensions = req_ext

[ dn ]
C = CH
O = $ORGNAME
CN = $FQDN

[ req_ext ]
subjectAltName = $ALTNAMES
EOF

cat >server.cnf << EOF
FQDN = $HOST_PORT
ORGNAME = ParsecTest
ALTNAMES = DNS:\$FQDN   # , DNS:bar.example.org , DNS:www.foo.example.org

# --- no modifications required below ---
[ req ]
default_bits = 2048
default_md = sha256
prompt = no
encrypt_key = no
distinguished_name = dn
req_extensions = req_ext

[ dn ]
C = CH
O = \$ORGNAME
CN = \$FQDN

[ req_ext ]
subjectAltName = \$ALTNAMES
EOF

# CA
echo "creating CA key"
openssl genrsa -out ca.key 4096
echo "creating CA cert"
openssl req -new -x509 -days 10 -config ca.cnf -key ca.key -out ca.crt
# Client
echo "creating client key"
openssl genrsa -out client.key 4096
openssl rsa -in client.key -outform der -out client.der
echo "creating client csr"
openssl req -new -config client.cnf -key client.key -out client.csr
echo "signing client csr"
openssl x509 -req -days 10 -in client.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out client.crt

cat client.crt ca.crt > client_chain.crt

openssl pkcs12 -export -in client_chain.crt -inkey client.key \
               -out client.p12 -name client \
               -CAfile ca.crt -caname root -password "pass:${PASSWORD}"
rm -f client_cert.jks
keytool -import -file client_chain.crt -alias client \
       -keystore client_cert.jks -storepass "${PASSWORD}" -trustcacerts -noprompt
rm -f client.jks
keytool -importkeystore \
        -deststorepass "${PASSWORD}" -destkeypass "${PASSWORD}" -destkeystore client.jks \
        -srckeystore client.p12 -srcstoretype PKCS12 -srcstorepass "${PASSWORD}" \
        -alias client

# Server
echo "creating server key"
openssl genrsa -out server.key 4096
echo "creating server csr"
openssl req -new -config server.cnf -key server.key -out server.csr
echo "signing server csr"
openssl x509 -req -days 10 -in server.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out server.crt

# Server Chain
cat server.crt ca.crt > server_chain.crt
# writing java truststore
rm -f server_chain.jks
keytool -import -file server_chain.crt -alias server \
       -keystore server_chain.jks -storepass "${PASSWORD}" -trustcacerts -noprompt

# reload nginx
nginx -s reload
echo
echo "DONE"