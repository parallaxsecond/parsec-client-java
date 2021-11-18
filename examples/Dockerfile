FROM ubuntu:latest
WORKDIR "/home/ggc_user"

# Replace the args to lock to a specific version


ARG GREENGRASS_RELEASE_VERSION=2.5.0
ARG GREENGRASS_ZIP_FILE=greengrass-${GREENGRASS_RELEASE_VERSION}.zip
ARG GREENGRASS_RELEASE_URI=https://d2s8p88vqu9w66.cloudfront.net/releases/${GREENGRASS_ZIP_FILE}
ARG GREENGRASS_ZIP_SHA256=greengrass.zip.sha256

# Author
LABEL maintainer="56K IoT Team!"
# Greengrass Version
LABEL greengrass-version=${GREENGRASS_RELEASE_VERSION}

# Set up Greengrass v2 execution parameters
# TINI_KILL_PROCESS_GROUP allows forwarding SIGTERM to all PIDs in the PID group so Greengrass can exit gracefully
ENV TINI_KILL_PROCESS_GROUP=1 \
    GGC_ROOT_PATH=gg_root \
    PROVISION=false \
    AWS_REGION=us-east-1 \
    AWS_ACCESS_KEY_ID \
    AWS_SECRET_ACCESS_KEY \
    THING_NAME=`date +%s`-gg-docker \

RUN [eval $(aws sts assume-role --role-arn arn:aws:iam::261610780523:role/OrganizationAccountAccessRole --role-session-name "rb5tests" | jq -r '.Credentials | "export AWS_ACCESS_KEY_ID=\(.AccessKeyId)\nexport AWS_SECRET_ACCESS_KEY=\(.SecretAccessKey)\nexport AWS_SESSION_TOKEN=\(.SessionToken)\n"')]
ADD start.sh
RUN start.sh
COPY  ../../aws-greengrass-pkcs11-provider/target/pkcs11-provider-2.0.0-SNAPSHOT.jar pkcs11-provider-2.0.0-SNAPSHOT.jar
CMD [java -Droot="$GGC_ROOT_PATH" -Dlog.store=FILE -jar lib/Greengrass.jar --aws-region eu-central-1 --thing-name gg-eulach --thing-group-name GreengrassQuickStartGroup --component-default-user ggc_user:ggc_group --provision ${PROVISION} --setup-system-service false --deploy-dev-tools true --init-config config.yaml --trusted-plugin pkcs11-provider-2.0.0-SNAPSHOT.jar]