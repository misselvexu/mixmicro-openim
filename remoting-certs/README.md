```bash
# create new ca (password:1qaz2wsx)

./CA.sh -newca

# Server Cert
openssl genrsa -out server/server-key.pem 2048

openssl req -new -out server/server-req.csr -key server/server-key.pem

openssl x509 -req -in server/server-req.csr -out server/server-cert.pem -signkey server/server-key.pem -CA demoCA/cacert.pem -CAkey demoCA/private/cakey.pem -CAcreateserial -days 3650


# Client Cert
openssl genrsa -out client/client-key.pem 2048

openssl req -new -out client/client-req.csr -key client/client-key.pem

openssl x509 -req -in client/client-req.csr -out client/client-cert.pem -signkey client/client-key.pem -CA demoCA/cacert.pem -CAkey demoCA/private/cakey.pem -CAcreateserial -days 3650


# client-cert.pem -> p12
openssl pkcs12 -export -in client-cert.pem -out client.p12 -inkey client-key.pem

# client p12 -> keystore.jks
keytool -importkeystore -srckeystore client.p12 -srcstoretype PKCS12 -deststoretype JKS -destkeystore keystore.jks

# server cert -> server.cer
openssl x509 -inform pem -in server-cert.pem -outform der -out server.cer

# import server cer into client keystore.jks
keytool -import -alias serverkey -keystore keystore.jks -file server.cer

```