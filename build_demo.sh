#!/bin/bash
set -e
pushd $(dirname $0)
md5_cmd=md5

if ! test -x /sbin/md5; then
  md5_cmd=md5sum
fi
GG_THING_NAME=$(id -un)-greengrass-parsec
if test -e /etc/hostname; then
  GG_THING_NAME=$(cat /etc/hostname)-greengrass-parsec
fi

function dirty_build_on_new_comits() {
  for repo in \
    awslabs/aws-crt-java \
    aws/aws-iot-device-sdk-java-v2 \
    revaultch/aws-greengrass-nucleus; do
  curl -S https://api.github.com/repos/${repo}/commits/key-op-prototype
  done | ${md5_cmd} | cut -d" " -f1 > greengrass_demo/dirty_repo.txt
  touch -t 190001010000 greengrass_demo/dirty_repo.txt
  export DIRTY_TS=$(cat greengrass_demo/dirty_repo.txt)
}

function build_greengrass_patched() {
pushd examples/greengrass/parsec-greengrass-run-config/docker/
docker build . \
       --build-arg BUILD_TS=${DIRTY_TS} \
       --tag parallaxsecond/greengrass_patched:latest \
       --progress plain
popd
}
function copy_deps_from_greengrass_patched_to_local() {
  docker run -v ~/.m2/repository:/host_m2_repository parallaxsecond/greengrass_patched:latest \
  /bin/bash -c "cp -r ~/.m2/repository/* /host_m2_repository"
}

function build_parsec_containers() {
pushd ./parsec-testcontainers/
./build.sh
popd
}

function build_greengrass_with_provider() {
  docker build . -f greengrass_demo/Dockerfile --tag parallaxsecond/greengrass_demo:latest  --progress plain
}

function parsec_run() {
    docker rm -f parsec_docker_run 2> /dev/null
    docker run -d --name parsec_docker_run \
          -ti \
          -v GG_PARSEC_STORE:/var/lib/parsec/mappings \
          -v GG_PARSEC_SOCK:/run/parsec \
           parallaxsecond/parsec:0.8.1
}
function gg_run() {
  docker rm -f "${1}" 2> /dev/null
  
  # shellcheck disable=SC2086
  docker run ${3} --name "${1}" \
         -e GG_THING_NAME="${GG_THING_NAME}" \
         -e GG_ADDITIONAL_CMD_ARGS="--trusted-plugin /provider.jar" \
         -e AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID}" \
         -e AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY}" \
         -e AWS_REGION="${AWS_REGION}" \
	 -e AWS_SESSION_TOKEN="${AWS_SESSION_TOKEN}" \
         -v GG_PARSEC_SOCK:/run/parsec \
         -v GG_HOME:/home/ggc_user \
	 -p 1441:1441 -p 1442:1442 \
         parallaxsecond/greengrass_demo:latest "${2}"
}
function run_demo() {
  parsec_run
  source secrets.env
  gg_run greengrass_demo_provisioning provision
  gg_run greengrass_demo_run run -d
  docker logs -f greengrass_demo_run
}

function build() {
  echo "Starting build ..."
  dirty_build_on_new_comits
  build_greengrass_patched
  copy_deps_from_greengrass_patched_to_local
  build_parsec_containers
  build_greengrass_with_provider
  echo "Build Done."
}
if [ "${1}" == "" ]; then
  build
  run_demo
else
  ${1}
fi
