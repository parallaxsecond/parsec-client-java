#!/bin/bash -e
function docker_arch() {
  case "${arch}" in
    amd64*|x86_64*|x64*)
      echo "linux/amd64"
    ;;
    arm64*|aarch64*)
        echo "linux/arm64"
    ;;
    *)
      echo "linux/$(arch)"
    ;;
  esac
}
PLATFORMS="$(docker_arch)"
PUSH="false"

export REGISTRY=parallaxsecond
export DOCKER_CLI_EXPERIMENTAL=enabled
docker run --privileged --rm tonistiigi/binfmt --install all
BUILDX_BASE="docker buildx bake --progress plain --set *.platform=${PLATFORMS}"
BUILDX_BUILDER="${BUILDX_BASE} --set *.output=type=image,push=false -f docker-compose-builder.yml"
BUILDX="${BUILDX_BASE} --set *.output=type=image,push=${PUSH} -f docker-compose.yml"

${BUILDX_BUILDER} parsec-rust-builder
${BUILDX_BUILDER}
${BUILDX}

