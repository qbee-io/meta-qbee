rpi_install_firmware_to_rootfs() {
  install -d ${IMAGE_ROOTFS}/boot.image/overlays

  cp -a ${DEPLOY_DIR_IMAGE}/${BOOTFILES_DIR_NAME}/* ${IMAGE_ROOTFS}/boot.image/

  find ${DEPLOY_DIR_IMAGE}/ -type f \( -iname "*.dtb" \) \( ! -iname "*+git*+*" \) -exec cp {} ${IMAGE_ROOTFS}/boot.image/ \;
  find ${DEPLOY_DIR_IMAGE}/ -type f \( -iname "*.dtbo" \) \( ! -iname "*+git*+*" \) -exec cp {} ${IMAGE_ROOTFS}/boot.image/overlays/ \;

  if [ -f ${DEPLOY_DIR_IMAGE}/u-boot.bin ]; then
    cp ${DEPLOY_DIR_IMAGE}/u-boot.bin ${IMAGE_ROOTFS}/boot.image/${SDIMG_KERNELIMAGE}
  fi

  if [ -f ${DEPLOY_DIR_IMAGE}/Image ]; then
    cp ${DEPLOY_DIR_IMAGE}/Image ${IMAGE_ROOTFS}/boot.image
  fi

  if [ -f ${DEPLOY_DIR_IMAGE}/boot.scr ]; then
    cp ${DEPLOY_DIR_IMAGE}/boot.scr ${IMAGE_ROOTFS}/boot.image
  fi
}

ROOTFS_POSTPROCESS_COMMAND += "rpi_install_firmware_to_rootfs; "
