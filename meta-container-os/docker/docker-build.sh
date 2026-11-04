#!/usr/bin/env bash

set -e
BASEDIR=$(cd $(dirname $0) && pwd)
DOCKER_TAG="${DOCKER_TAG:-wrynose-6.0.4}"

YOCTO_DEPLOY_DIR="${YOCTO_DEPLOY_DIR:-$BASEDIR/../../build/tmp/deploy/images/qemux86-64}"

BASE_IMAGE="$YOCTO_DEPLOY_DIR/core-image-minimal-qemux86-64.rootfs.wic"
OVMF_IMAGE="$YOCTO_DEPLOY_DIR/ovmf.qcow2"

cp "$BASE_IMAGE" "$BASEDIR/images/vmimage.wic"
cp "$OVMF_IMAGE" "$BASEDIR/images/ovmf.qcow2"

docker build -t qbeeio/qbee-demo:$DOCKER_TAG .
