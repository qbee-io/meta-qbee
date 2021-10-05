SUMMARY = "bitbake-layers recipe"
DESCRIPTION = "Recipe created by bitbake-layers"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-3.0-only;md5=c79ff39f19dfec6d293b95dea7b07891"

RDEPENDS_${PN} += "cfengine openvpn curl coreutils bash gawk openssl-bin procps systemd-extra-utils kernel-modules sshd"

inherit systemd

SRC_URI = "file://qbee/ \
  file://COPYRIGHT \
  "

qbee_dest_path = "/opt/qbee"
cf_workdir = "${localstatedir}/lib/qbee"

FILES_${PN} = "${qbee_dest_path}/* ${cf_workdir}/bin/* ${systemd_system_unitdir}/*" 

SYSTEMD_SERVICE_${PN} = "qbee-agent.service"

do_install () {

  mkdir -p ${D}${qbee_dest_path}/bin
  for file in ${WORKDIR}/qbee/bin/*; do
    install -m 755 $file ${D}${qbee_dest_path}/bin/$(basename $file)
  done

  mkdir -p ${D}${qbee_dest_path}/share/qbee
  for file in ${WORKDIR}/qbee/share/qbee/*; do
    install -m 644 $file ${D}${qbee_dest_path}/share/qbee/$(basename $file)
  done

  mkdir -p ${D}${cf_workdir}/bin/
  for file in ${D}${qbee_dest_path}/bin/*; do
    ln -s ${qbee_dest_path}/bin/$(basename $file) ${D}${cf_workdir}/bin/
  done

  mkdir -p ${D}${systemd_system_unitdir}
  for file in ${WORKDIR}/qbee/systemd/*.service; do
    sed -i -e "s#@cf_workdir@#${cf_workdir}#g" $file
    install -m 644 $file ${D}${systemd_system_unitdir}/$(basename $file)
  done
}
