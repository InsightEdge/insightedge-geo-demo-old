#!/usr/bin/env bash

SCRIPTS_DIR=$(dirname "$0")

pushd $SCRIPTS_DIR/..

echo "Building feeder fat jar"
sbt feeder/assembly

echo "Building InsightEdge Processing fat jar"
sbt insightedgeProcessing/assembly

popd
