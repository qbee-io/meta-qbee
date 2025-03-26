#!/usr/bin/env bash

set -e
BASEDIR=$(cd $(dirname $0) && pwd)
DOCKER_TAG="${DOCKER_TAG:-2024.50-scarthgap}"

YOCTO_DEPLOY_DIR="/media/jonhenrik/NOLA_EXTRA/yocto/scarthgap/cache/tmp/qemux86-64/deploy/images/qemux86-64/"

BASE_IMAGE="$YOCTO_DEPLOY_DIR/core-image-minimal-qemux86-64.rootfs.wic"
OVMF_IMAGE="$YOCTO_DEPLOY_DIR/ovmf.qcow2"

cp "$BASE_IMAGE" "$BASEDIR/images/vmimage.wic"
cp "$OVMF_IMAGE" "$BASEDIR/images/ovmf.qcow2"

docker build -t qbeeio/qbee-demo:$DOCKER_TAG .
