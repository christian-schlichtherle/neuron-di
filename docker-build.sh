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
        christianschlichtherle/scala-sbt:$1 \
        sbt $2
}

set -ex
sbt ${1:-1.2.8-jdk11} +test:compile
sbt ${2:-1.2.8-jdk8} +test
