#!/usr/bin/env bash

function sbt() {
    docker run \
        --env GUICE_VERSION \
        --interactive \
        --rm \
        --tty \
        --volume $HOME/.ivy2:/root/.ivy2 \
        --volume $HOME/.sbt:/root/.sbt \
        --volume $PWD:/workspace \
        --workdir /workspace \
        hseeberger/scala-sbt:$1 \
        sbt $2
}

set -ex
sbt ${1:-11.0.2_2.12.8_1.2.8} +test:compile
sbt ${2:-8u212_2.12.8_1.2.8} +test
