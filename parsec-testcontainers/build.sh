#!/bin/bash -e
docker_cache=parsec_docker_cache

CACHE_CONFIG=""
if (docker buildx inspect | grep "Driver: docker-container"); then
  CACHE_CONFIG=" --set *.cache-from=type=local,src=${docker_cache} --set *.cache-to=mode=max,type=local,dest=${docker_cache}_new"
fi

echo "docker buildx command:"
echo "docker buildx bake ${CACHE_CONFIG} --progress plain --load"

# shellcheck disable=SC2086
docker buildx bake ${CACHE_CONFIG} \
  --progress plain \
  --load

rm -rf ${docker_cache} || true
if [ -d "${docker_cache}_new" ]; then
  mv ${docker_cache}_new ${docker_cache} || true
fi
