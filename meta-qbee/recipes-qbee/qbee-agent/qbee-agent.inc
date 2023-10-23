DESCRIPTION = "Installs the qbee-agent pre-compiled binaries onto a Yocto image"
HOMEPAGE = "https://qbee.io/"

# The bootstrap key to use for automatic bootstrap
QBEE_BOOTSTRAP_KEY ?= ""
# The device name. Unset variable means that qbee will use hostname. Supported values are "machine-id" and "mac-address"
QBEE_DEVICE_NAME_TYPE ?= ""
# device hub host
QBEE_DEVICE_HUB_HOST ?= "device.app.qbee.io"
# vpn server host
QBEE_VPN_SERVER ?= "vpn.app.qbee.io"
# disable remote access locally, needs to explicitly be set to true if remote access should be disabled
QBEE_DISABLE_REMOTE_ACCESS ?= ""
# directory where qbee stores its config and keys (needs to be read/write)
QBEE_CONF_DIR ?= "/data/qbee/etc"
# directory where qbee stores its state (needs to be read/write)
QBEE_STATE_DIR ?= "/data/qbee/var"
# whether or not qbee-agent should remove any bootstrap seeding info, (ie. remove /etc/qbee/yocto/.bootstrap_env)
# will only work on systems with read/write rootfs
QBEE_CLEAN_SEEDING_INFO ?= "false"

RDEPENDS:${PN} += "coreutils bash kernel-modules sshd iptables"

B = "${WORKDIR}/build"

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
  install -m 0755 -d ${D}${bindir}/ 
  install -m 0700 -d ${D}${sysconfdir}/qbee
  install -m 0700 -d ${D}${sysconfdir}/qbee/yocto
  install -m 0600 ${S}/share/ssl/ca.cert ${D}${sysconfdir}/qbee/yocto

  install -m 0755 -d ${D}/opt/qbee/bin
  install -m 0755 ${S}/bin/qbee-agent ${D}/opt/qbee/bin
  install -m 0755 ${S}/bin/qbee-bootstrap ${D}/opt/qbee/bin

  install -m 0755 ${S}/qbee-agent-${HOST_GOARCH} ${D}${bindir}/qbee-agent

  sed -e 's#[@]QBEE_CONF_DIR[@]#${QBEE_CONF_DIR}#' \
    ${WORKDIR}/qbee-bootstrap-prep.sh.in > ${WORKDIR}/qbee-bootstrap-prep.sh
  install -m 0700 ${WORKDIR}/qbee-bootstrap-prep.sh ${D}/${sysconfdir}/qbee/yocto

  if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
    install -d ${D}/${systemd_unitdir}/system
    sed -e 's#[@]QBEE_CONF_DIR[@]#${QBEE_CONF_DIR}#;s#[@]QBEE_STATE_DIR[@]#${QBEE_STATE_DIR}#' \
      ${WORKDIR}/qbee-agent.service.in > ${WORKDIR}/qbee-agent.service
    install -m 0644 ${WORKDIR}/qbee-agent.service ${D}/${systemd_unitdir}/system/qbee-agent.service
  fi

  if ${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', 'true', 'false', d)}; then
    install -d ${D}${INIT_D_DIR}
    sed -e 's#[@]QBEE_CONF_DIR[@]#${QBEE_CONF_DIR}#;s#[@]QBEE_STATE_DIR[@]#${QBEE_STATE_DIR}#' \
      ${WORKDIR}/qbee-agent.init.in > ${WORKDIR}/qbee-agent.init
    install -m 0755 ${WORKDIR}/qbee-agent.init ${D}/${INIT_D_DIR}/qbee-agent
  fi

  install -m 0700 ${WORKDIR}/qbee-bootstrap-prep.sh ${D}/${sysconfdir}/qbee/yocto
  
  if [ ! -z "${QBEE_BOOTSTRAP_KEY}" ]; then
    cat > ${B}/bootstrap-env << EOF
BOOTSTRAP_KEY=${QBEE_BOOTSTRAP_KEY}
DEVICE_NAME_TYPE=${QBEE_DEVICE_NAME_TYPE}
DEVICE_HUB_HOST=${QBEE_DEVICE_HUB_HOST}
VPN_SERVER=${QBEE_VPN_SERVER}
CLEAN_SEEDING_INFO=${QBEE_CLEAN_SEEDING_INFO}
DISABLE_REMOTE_ACCESS=${QBEE_DISABLE_REMOTE_ACCESS}
EOF
  fi

  if [ -f ${B}/bootstrap-env ]; then
    install -m 0600 ${B}/bootstrap-env ${D}${sysconfdir}/qbee/yocto/.bootstrap-env
  fi
}

