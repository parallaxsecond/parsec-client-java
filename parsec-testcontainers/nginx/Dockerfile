# image used to have both openssl and keytool to generate keystores for testing
# e.g. openssl pkcs12 cannot add bag attributes required for Java trust stores
FROM nginx
RUN apt update && apt -y install default-jre