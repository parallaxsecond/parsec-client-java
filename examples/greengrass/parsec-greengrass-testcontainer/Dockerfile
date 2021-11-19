FROM ubuntu:latest
WORKDIR "/home/ggc_user"

# Build me with some build-args (load AWS credentials into local env):
# docker build --build-arg AWS_ACCESS_KEY_ID --build-arg AWS_SECRET_ACCESS_KEY --build-arg THING_NAME=`date +%s`-gg-docker .

LABEL maintainer="56K IoT Team!"

ARG DEBIAN_FRONTEND=noninteractive
RUN apt update && apt install -y openjdk-8-jre awscli softhsm2 jq curl unzip less

# Pass AWS Credentials into docker build with --build-args
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY
ARG AWS_REGION
ARG THING_NAME
##  GreenGrass
ARG GREENGRASS_RELEASE_VERSION=2.5.0
ARG GREENGRASS_ZIP_FILE=greengrass-${GREENGRASS_RELEASE_VERSION}.zip
ARG GREENGRASS_RELEASE_URI=https://d2s8p88vqu9w66.cloudfront.net/releases/${GREENGRASS_ZIP_FILE}
ARG GREENGRASS_ZIP_SHA256=greengrass.zip.sha256
LABEL greengrass-version=${GREENGRASS_RELEASE_VERSION}


# Set up Greengrass v2 execution parameters
# TINI_KILL_PROCESS_GROUP allows forwarding SIGTERM to all PIDs in the PID group so Greengrass can exit gracefully
ENV TINI_KILL_PROCESS_GROUP=1 \
    PROVISION=true \
    AWS_REGION=${AWS_REGION} \
    AWS_DEFAULT_REGION=${AWS_REGION} \
    THING_NAME=${THING_NAME} \
    AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} \
    AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}

# RUN eval $(aws sts assume-role --role-arn arn:aws:iam::261610780523:role/OrganizationAccountAccessRole --role-session-name "rb5tests" | jq -r '.Credentials | "export AWS_ACCESS_KEY_ID=\(.AccessKeyId)\nexport AWS_SECRET_ACCESS_KEY=\(.SecretAccessKey)\nexport AWS_SESSION_TOKEN=\(.SessionToken)\n"')
COPY start.sh .
RUN pwd && ls && ./start.sh
COPY  pkcs11-provider-2.0.0-SNAPSHOT.jar pkcs11-provider-2.0.0-SNAPSHOT.jar
RUN java -Droot=/home/ggc_user/gg_root -Dlog.store=FILE -jar /home/ggc_user/greengrass/lib/Greengrass.jar --aws-region ${AWS_REGION} --thing-name ${THING_NAME} --thing-group-name GreengrassQuickStartGroup --component-default-user ggc_user:ggc_group --provision ${PROVISION} --setup-system-service false --deploy-dev-tools true --init-config /home/ggc_user/greengrass/config.yaml --trusted-plugin pkcs11-provider-2.0.0-SNAPSHOT.jar --start false
ENV PROVISION=false