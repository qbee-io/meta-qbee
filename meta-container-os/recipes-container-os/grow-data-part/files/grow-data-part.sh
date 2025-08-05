#!/bin/sh

set -e

# All sizes are in blocks of 512 bytes

DATA_PART=$(grep -v ^# /etc/fstab  | grep '/data' | head -n 1 | awk '{print $1}')

data_part_base=$(basename ${DATA_PART})
storage_device_base=$(basename $(readlink -f /sys/class/block/${data_part_base}/..))
storage_device=/dev/${storage_device_base}

total_disk_size=$(cat /sys/block/${storage_device_base}/size)

data_part_size=$(cat /sys/block/${storage_device_base}/${data_part_base}/size)
data_part_start=$(cat /sys/block/${storage_device_base}/${data_part_base}/start)

if [ -z "${total_disk_size}" ] || [ -z "${data_part_size}" ] || [ -z "${data_part_start}" ]; then
    echo "Failed to read disk sizes. Aborting..."
    exit 1
fi

data_part_end=$(expr ${data_part_start} + ${data_part_size})

# expr returns 1 (error) if the result is 0, which terminates the script
# because of 'set -e'. Silence this error
free_space=$(expr ${total_disk_size} - ${data_part_end} || true)

# If there is less than 8196 blocks = 4 MiB free unused space, we consider
# the disk as already resized. After resizing, some disk space may still
# have been left unused.
if [ ${free_space} -lt 8196 ]; then
    echo "Disk has already been resized."
    exit 0
fi

# Parted will refuse to resize the parition because it needs to re-write the
# partition table and will refuse to do so unless there is a backup. This
# ensures that GPT backup headers are written to the end of the disk.
# Note: On some MBR systems using BusyBox's fdisk this call will fail,
# this is OK (MBR systems don't have a backup header), always return true. 
echo "w" | fdisk ${storage_device} &> /dev/null || true

/usr/sbin/parted -s ${storage_device} resizepart 4 100%

# Modifying the partition table in any way may trigger udev at some point.
# We don't want to continue right while udev is recreating /dev/disk symlinks.
# Instead, we want this to happen deterministically now and be done when the script is done.
/usr/sbin/partprobe
if [ -x /sbin/udevadm ]; then
    /sbin/udevadm settle
fi
