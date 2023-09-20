DESCRIPTION = "Installs the qbee-agent pre-compiled binaries onto a Yocto image"
HOMEPAGE = "https://qbee.io/"

LICENSE="CLOSED"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=011d72da1f3ad57389b69c20c8a419d5"

RDEPENDS:${PN} += "coreutils bash kernel-modules sshd iptables"

SRC_URI = "https://cdn.qbee.io/software/qbee-agent/${PV}/binaries/qbee-agent-${PV}.tar.gz \
  file://LICENSE \
  "

SRC_URI[sha256sum] = "35ac22b0822d4c33f97fc9301f4834038d0e421c6adc975393920b9e801834e8"

inherit update-rc.d systemd goarch

INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

SYSTEMD_SERVICE:${PN} = "qbee-agent.service"

INITSCRIPT_PACKAGES = "${PN}"
INITSCRIPT_NAME:${PN} = "qbee-agent"
INITSCRIPT_PARAMS:${PN} = "defaults 90 10"

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

