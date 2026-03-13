SUMMARY = "Grow data partition service"
LICENSE = "CLOSED"

LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://grow-data-part.sh \
  file://grow-data-part.service"

S = "${WORKDIR}"

inherit systemd

SYSTEMD_PACKAGES += "${PN}"
SYSTEMD_SERVICE:${PN} = "grow-data-part.service"

do_install() {
  install -d ${D}${systemd_unitdir}/system/
  install -m 0644 ${WORKDIR}/grow-data-part.service ${D}${systemd_unitdir}/system/
  install -d ${D}/usr/bin/
  install -m 0755 ${WORKDIR}/grow-data-part.sh ${D}/usr/bin/
}

FILES:${PN} += "/usr/bin/grow-data-part.sh \
${systemd_unitdir}/system/grow-data-part.service"

