DESCRIPTION = "This is a simple example recipe that cross-compiles a Go program."
HOMEPAGE = "https://qbee.io/"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

RDEPENDS:${PN} += "coreutils bash kernel-module-tun sshd iptables"

SRC_URI = "https://cdn.qbee.io/software/qbee-agent/${PV}/binaries/qbee-agent-${PV}.tar.gz \
           file://qbee-agent.service \
           file://qbee-agent.init \
           file://ca.cert \
           file://qbee-agent \
           file://qbee-bootstrap"

SRC_URI[sha256sum] = "71d62dc779200b776c57580b2e29d5eba5a7b7ff2a7389062bbf731f58cb6e17"

inherit update-rc.d systemd
#inherit systemd

INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

GO_ARCH:x86-64 = "amd64"
GO_ARCH:i586 = "386"
GO_ARCH:i686 = "386"
GO_ARCH:armv7l = "arm"
GO_ARCH:aarch64 = "arm64"

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
  install -m 600 ${WORKDIR}/ca.cert ${D}${sysconfdir}/qbee/ppkeys

  install -m 755 -d ${D}/opt/qbee/bin
  install -m 755 ${WORKDIR}/qbee-agent ${D}/opt/qbee/bin
  install -m 755 ${WORKDIR}/qbee-bootstrap ${D}/opt/qbee/bin

  install -m 755 ${S}/qbee-agent-${GO_ARCH} ${D}${bindir}/qbee-agent

  if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
    install -d ${D}/${systemd_unitdir}/system
    install -m 644 ${WORKDIR}/qbee-agent.service ${D}/${systemd_unitdir}/system
  fi

  if ${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', 'true', 'false', d)}; then
    install -d ${D}${INIT_D_DIR}
    install -m 755 ${WORKDIR}/qbee-agent.init ${D}/${INIT_D_DIR}/qbee-agent
  fi
}

