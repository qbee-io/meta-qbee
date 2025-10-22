#!/bin/sh

set -e

case "$1" in
  slot-post-install)
     test "$RAUC_SLOT_CLASS" = "rootfs" || exit 0
     echo "$RAUC_SLOT_MOUNT_POINT" >> /data/rauc-mount-point
    ;;
  *)
    exit 1
    ;;
esac

exit 0
