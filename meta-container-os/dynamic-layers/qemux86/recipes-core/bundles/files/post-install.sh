#!/bin/sh

set -e

case "$1" in
  slot-post-install)
    test "$RAUC_SLOT_CLASS" = "rootfs" || exit 0
    echo "[RAUC Hook] Post install script executed for rootfs slot."
    # Additional post-installation steps can be added here
    ;;
  *)
    exit 1
    ;;
esac

exit 0
