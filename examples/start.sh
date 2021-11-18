#!/bin/bash
#Install AWS CLI
apt-get install openjdk-8-jre
apt-get install aws-cli
#Install softhsm2
apt-get install softhsm2
echo "directories.tokendir = /var/lib/softhsm/tokens" > softhsm2.conf
export SOFTHSM2_CONF=/var/lib/softhsm/softhsm2.conf
softhsm2-util --init-token --slot 0001 --label "ggeulachtoken" --so-ping 1234 --pin 1234
softhsm2-util --show-slots
# Download, install, and verify Greengrass latest (2.5.0)
mkdir greengrass
cd greengrass
curl -s https://d2s8p88vqu9w66.cloudfront.net/releases/greengrass-nucleus-latest.zip > greengrass-nucleus-latest.zip
unzip greengrass-nucleus-latest.zip
java -jar lib/Greengrass.jar --version
# prepare config and generate thing name
cat << EOF > config.yaml
system:
  certificateFilePath: "pkcs11:object=ggeulachtoken;type=cert"
  #privateKeyPath: "${GGC_ROOT_PATH}/private.pem.key"
  # the object in the URI is the actual label given the private key
  privateKeyPath: "pkcs11:object=ggeulachtoken;type=private"
  rootCaPath: "${GGC_ROOT_PATH}/AmazonRootCA1.pem"
  rootpath: ""
  thingName: "$THING_NAME"
services:
  aws.greengrass.Nucleus:
    componentType: "NUCLEUS"
    version: "2.5.0"
    configuration:
      awsRegion: "eu-central-1"
      iotRoleAlias: "GreengrassCoreTokenExchangeRoleAlias"
      iotDataEndpoint: "device-data-prefix-ats.iot.eu-central-1.amazonaws.com"
      iotCredEndpoint: "device-credentials-prefix.credentials.iot.eu-central-1.amazonaws.com"
  aws.greengrass.crypto.Pkcs11Provider:
     configuration:
       # requied, but value doesn't matter, labelling the backend of the provider
       name: softhsm2
       library: /usr/lib/softhsm/libsofthsm2.so
       slot: 0001
       userPin: 1234
EOF
#java -Droot="${GGC_ROOT_PATH}" -Dlog.store=FILE -jar lib/Greengrass.jar --aws-region eu-central-1 --thing-name gg-eulach --thing-group-name GreengrassQuickStartGroup --component-default-user ggc_user:ggc_group --provision ${PROVISION} --setup-system-service false --deploy-dev-tools true --init-config ../../gg-iotapp/config-pkcs11.yaml --trusted-plugin ../../aws-greengrass-pkcs11-provider/target/pkcs11-provider-2.0.0-SNAPSHOT.jar