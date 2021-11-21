#!/bin/bash
pushd $(dirname $0)
pushd examples/greengrass/parsec-greengrass-run-config/docker/
pwd
docker build .  --tag parallaxsecond/greengrass_demo:latest
popd

pushd ./parsec-testcontainers/
./build.sh
popd


docker build . -f greengrass_demo/Dockerfile

