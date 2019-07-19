#!/usr/bin/env bash

function docker-sbt() {
    local image_tag=$1
    shift
    docker run \
        --env GUICE_VERSION \
        --interactive \
        --rm \
        --tty \
        --volume $HOME/.ivy2:/root/.ivy2 \
        --volume $HOME/.sbt:/root/.sbt \
        --volume $PWD:/workdir \
        --workdir /workdir \
        openjdk:$image_tag \
        ./sbt $@
}

set -ex
docker-sbt ${1:-9-jdk-slim} +test:compile
docker-sbt ${2:-8-jdk-slim} +test
