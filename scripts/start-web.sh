#!/usr/bin/env bash

SCRIPTS_DIR=$(dirname "$0")

pushd $SCRIPTS_DIR/..

sbt web/run

popd

