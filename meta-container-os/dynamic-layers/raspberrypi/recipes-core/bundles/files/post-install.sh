#!/bin/sh

set -e

# we should detect existing file and cmp to avoid unnecessary writes if possible
safe_copy() {
  if [ $# -gt 2 ]; then
    echo "safe_copy can only handle one file copy at a time" >&2
    exit 2
  fi

  # do not copy if the files are the same
  if cmp -s "$1" "$2"; then
    return
  fi

  cp -a "$1" "$2".tmp || return $?
  sync "$2".tmp || return $?
  mv "$2".tmp "$2" || return $?
  sync "$(dirname "$2")" || return $?
}

update_firmware() {
  RAUC_MOUNT_POINT="$1"
  echo "[RAUC Hook] Updating firmware files from $RAUC_MOUNT_POINT/boot.image to /boot"

  # Copy 'core' firmware files first
  find "$RAUC_MOUNT_POINT/boot.image" -maxdepth 1 -type f ! -name "*.dtbo" | while IFS= read -r f; do
    safe_copy "$f" "/boot/$(basename "$f")"
  done

  # Copy overlays
  find "$RAUC_MOUNT_POINT/boot.image/overlays" -maxdepth 1 -type f -name "*.dtbo" | while IFS= read -r f; do
    safe_copy "$f" "/boot/overlays/$(basename "$f")"
  done
}

case "$1" in
  slot-post-install)
    test "$RAUC_SLOT_CLASS" = "rootfs" || exit 0
    update_firmware "$RAUC_SLOT_MOUNT_POINT"
    ;;
  *)
    exit 1
    ;;
esac

exit 0
