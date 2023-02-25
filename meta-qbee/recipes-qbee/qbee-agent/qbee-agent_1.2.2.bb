SUMMARY = "bitbake-layers recipe"
DESCRIPTION = "Recipe created by bitbake-layers"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${WORKDIR}/COPYRIGHT;md5=ce6267074a0b0daba74d60688fbecb92"

RDEPENDS:${PN} += "cfengine openvpn curl coreutils bash gawk openssl procps systemd-extra-utils kernel-modules sshd iptables"

inherit systemd

SRC_URI = "file://qbee/ \
  file://COPYRIGHT \
  "

qbee_dest_path = "/opt/qbee"
cf_workdir = "/var/lib/qbee"

FILES:${PN} = "${qbee_dest_path}/* ${cf_workdir}/bin/* ${systemd_system_unitdir}/*" 

SYSTEMD_SERVICE:${PN} = "qbee-agent.service"

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
