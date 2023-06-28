DESCRIPTION = "This is a simple example recipe that cross-compiles a Go program."
HOMEPAGE = "https://qbee.io/"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

#RDEPENDS:${PN} += "coreutils bash kernel-module-nf-conntrack kernel-module-tun sshd iptables"
RDEPENDS:${PN} += "coreutils bash kernel-modules sshd iptables"

SRC_URI = "https://cdn.qbee.io/software/qbee-agent/${PV}/binaries/qbee-agent-${PV}.tar.gz"

SRC_URI[sha256sum] = "be6f169e0b800b5439f625ce3153532e171b774e56a8507487a0e050f0b5be3a"

inherit update-rc.d systemd goarch

INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

SYSTEMD_SERVICE:${PN} = "qbee-agent.service"
#SYSTEMD_AUTO_ENABLE = "enable"

INITSCRIPT_PACKAGES = "${PN}"
INITSCRIPT_NAME:${PN} = "qbee-agent"
INITSCRIPT_PARAMS:${PN} = "start 10 2 3 4 5 . stop 70 0 1 6 ."

FILES:${PN} += "/opt/qbee/* ${systemd_unitdir}/system/qbee-agent.service ${sysconfdir}/qbee/*"

do_install () {
  install -m 755 -d ${D}${bindir}/ 
  install -m 700 -d ${D}${sysconfdir}/qbee
  install -m 700 -d ${D}${sysconfdir}/qbee/ppkeys
  install -m 600 ${S}/share/ssl/ca.cert ${D}${sysconfdir}/qbee/ppkeys

  install -m 755 -d ${D}/opt/qbee/bin
  install -m 755 ${S}/bin/qbee-agent ${D}/opt/qbee/bin
  install -m 755 ${S}/bin/qbee-bootstrap ${D}/opt/qbee/bin

  install -m 755 ${S}/qbee-agent-${HOST_GOARCH} ${D}${bindir}/qbee-agent

  if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
    install -d ${D}/${systemd_unitdir}/system
    install -m 644 ${S}/init-scripts/systemd/qbee-agent.service ${D}/${systemd_unitdir}/system
  fi

  if ${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', 'true', 'false', d)}; then
    install -d ${D}${INIT_D_DIR}
    install -m 755 ${S}/init-scripts/sysvinit/qbee-agent ${D}/${INIT_D_DIR}/qbee-agent
  fi
}

