#!/bin/bash
pushd $(dirname $0)

function build_greengrass_patched() {
pushd examples/greengrass/parsec-greengrass-run-config/docker/
docker build . --tag parallaxsecond/greengrass_patched:latest
popd
}

function build_parsec_containers() {
pushd ./parsec-testcontainers/
./build.sh
popd
}

function build_greengrass_with_provider() {
  docker build . -f greengrass_demo/Dockerfile --tag parallaxsecond/greengrass_demo:latest
}

build_greengrass_patched
build_parsec_containers
build_greengrass_with_provider


docker rm -f parsec_docker_run > /dev/null
docker run -d --name parsec_docker_run \
       -ti \
       -v GG_PARSEC_STORE:/var/lib/parsec/mappings \
       -v GG_PARSEC_SOCK:/run/parsec \
       parallaxsecond/parsec:0.8.1


GG_THING_NAME=$(id -un)-gg-test

source secrets.env

function gg_run() {
docker rm -f "${1}" >/dev/null
# shellcheck disable=SC2086
docker run ${3} --name "${1}" \
       -e GG_THING_NAME="${GG_THING_NAME}" \
       -e GG_ADDITIONAL_CMD_ARGS="--trusted-plugin /provider.jar" \
       -e AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID}" \
       -e AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY}" \
       -e AWS_REGION="${AWS_REGION}" \
       -v GG_PARSEC_SOCK:/run/parsec \
       -v GG_HOME:/home/ggc_user \
       parallaxsecond/greengrass_demo:latest "${2}"
}

gg_run greengrass_demo_provisioning provision
gg_run greengrass_demo_run run -d

