#!/usr/bin/env bash

if [[ -z $BOOTSTRAP_KEY ]]; then
  echo "ERROR: No bootstrap key has been provided"
  exit 1
fi

BASEDIR=$(cd $(dirname $0) && pwd)
QBEE_TPM2_DIR="/var/lib/qbee-tpm2"

IMAGE="$BASEDIR/images/vmimage.wic"
FLASH_IMG="$BASEDIR/images/ovmf.qcow2"

MAC=$(echo $HOSTNAME | md5sum | sed 's/^\(..\)\(..\)\(..\)\(..\)\(..\).*$/02:\1:\2:\3:\4:\5/')

generate_user_password() {
  < /dev/urandom tr -dc A-Z-a-z-0-9 | head -c 8
  echo
}

setup_emulated_tpm() {
  mkdir -p "$QBEE_TPM2_DIR"
  swtpm socket --tpmstate dir="$QBEE_TPM2_DIR" --ctrl type=unixio,path="$QBEE_TPM2_DIR/swtpm-sock" --tpm2 --log level=20 -d
}

setup_emulated_tpm

QBEE_DEMO_DEVICE_HUB_HOST=${QBEE_DEMO_DEVICE_HUB_HOST:-device.app.qbee.io}

# Generate the bootstrap-env file for this device
cat > /run/.bootstrap-env << EOF
BOOTSTRAP_KEY=${BOOTSTRAP_KEY}
DEVICE_NAME_TYPE=mac-address
DEVICE_HUB_HOST=${QBEE_DEMO_DEVICE_HUB_HOST}
CLEAN_SEEDING_INFO=true
EOF


if [[ -c /dev/kvm ]]; then
  QEMU_OPTIONS="$QEMU_OPTIONS -machine type=pc,accel=kvm -smp 4 -cpu host"
fi

if [[ -S $QBEE_TPM2_DIR/swtpm-sock ]]; then
  QEMU_OPTIONS="$QEMU_OPTIONS -chardev socket,id=chrtpm,path=$QBEE_TPM2_DIR/swtpm-sock -tpmdev emulator,id=tpm0,chardev=chrtpm -device tpm-tis,tpmdev=tpm0"
  echo "TPM_DEVICE=/dev/tpm0" >> /run/.bootstrap-env
fi

/poky/scripts/wic cp /run/.bootstrap-env $IMAGE:4/etc/qbee/yocto/

qemu-system-x86_64 \
  -device virtio-net-pci,netdev=net0,mac=$MAC \
  -netdev user,id=net0,hostfwd=tcp::2222-:22 \
  -object rng-random,filename=/dev/urandom,id=rng0 \
  -device virtio-rng-pci,rng=rng0 \
  -drive if=none,id=hd,file=$IMAGE,format=raw \
  -device virtio-scsi-pci,id=scsi \
  -device scsi-hd,drive=hd \
  -usb -device usb-tablet \
  -drive if=pflash,format=qcow2,file=$FLASH_IMG \
  -cpu IvyBridge -machine q35 -smp 4 -m 1024 -serial mon:stdio -serial null -nographic \
  $QEMU_OPTIONS
