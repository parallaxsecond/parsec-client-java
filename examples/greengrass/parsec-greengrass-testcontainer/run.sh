#!/bin/bash

# retrieve your credentials here if needed
test -f secrets.sh && source secrets.sh
export AWS_REGION=eu-central-1

export DOCKER_NAME=gg-parsec
export THING_NAME=`whoami`-gg-parsec
DOCKER_BUILDKIT=0 docker build --build-arg AWS_ACCESS_KEY_ID --build-arg AWS_SECRET_ACCESS_KEY --build-arg THING_NAME --build-arg AWS_REGION -t ${DOCKER_NAME} .

echo "##########################################################################"
echo "# Execute this                                                           #"
echo "docker exec -it ${DOCKER_NAME}-container /bin/bash                       #"
echo "# and Check contents of gg_root/logs for log files in a different shell  #"
echo "##########################################################################"

docker run -p 1441:1441 -p 1442:1442 --rm -it --name ${DOCKER_NAME}-container ${DOCKER_NAME} java -Droot=/home/ggc_user/gg_root -Dlog.store=FILE -jar /home/ggc_user/greengrass/lib/Greengrass.jar --aws-region ${AWS_REGION} --thing-name ${THING_NAME} --thing-group-name GreengrassQuickStartGroup --component-default-user ggc_user:ggc_group --provision \${PROVISION} --setup-system-service false --deploy-dev-tools true --init-config /home/ggc_user/greengrass/config.yaml --trusted-plugin pkcs11-provider-2.0.0-SNAPSHOT.jar



