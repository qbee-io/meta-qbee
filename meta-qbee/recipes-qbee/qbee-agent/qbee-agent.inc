DESCRIPTION = "Installs the qbee-agent onto a Yocto image"
HOMEPAGE = "https://qbee.io/"

# The bootstrap key to use for automatic bootstrap
QBEE_BOOTSTRAP_KEY ?= ""
# The device name. Unset variable means that qbee will use hostname. Supported values are "machine-id" and "mac-address"
QBEE_DEVICE_NAME_TYPE ?= ""
# device hub host
QBEE_DEVICE_HUB_HOST ?= "device.app.qbee.io"
# disable remote access locally, needs to explicitly be set to true if remote access should be disabled
QBEE_DISABLE_REMOTE_ACCESS ?= ""
# directory where qbee stores its config and keys (needs to be read/write)
QBEE_CONF_DIR ?= "/data/qbee/etc"
# directory where qbee stores its state (needs to be read/write)
QBEE_STATE_DIR ?= "/data/qbee/var"
# whether or not qbee-agent should remove any bootstrap seeding info, (ie. remove /etc/qbee/yocto/.bootstrap_env)
# will only work on systems with read/write rootfs
QBEE_CLEAN_SEEDING_INFO ?= "false"
# Define a TPM device for qbee-agent to use
QBEE_TPM_DEVICE ?= ""
# Define a custom ca_cert bundle to use for private infrastructure
QBEE_CA_CERT ?= ""
# Define which systemd target qbee-agent should start after. Defaults to network.target as qbee-agent can work
# without network-online.target
QBEE_SYSTEMD_AFTER_TARGET ?= "network.target"
# Define which systemd target qbee-agent should start after. Defaults to network.target as qbee-agent can work
QBEE_SYSTEMD_WANTS_TARGET ?= "${QBEE_SYSTEMD_AFTER_TARGET}"
# the user to run qbee-agent as. If left empty, qbee-agent will run as root
QBEE_EXEC_USER ?= ""
# The elevation command to use for privileged operations if QBEE_EXEC_USER is set. Needs to be in json array format, e.g. '["/usr/bin/sudo", "-n"]'
QBEE_ELEVATION_COMMAND ?= ""
# Supplementary groups to add the execution user to for additional permissions. Needs to be in a space separated format, eg "docker tss"
QBEE_SUPPLEMENTARY_GROUPS ?= ""

RDEPENDS:${PN} += " \
  ca-certificates \
  "

RRECOMMENDS:${PN} += " \
  iptables \
  kernel-module-x-tables \
  kernel-module-ip-tables \
  kernel-module-iptable-filter \
  kernel-module-xt-tcpudp \
  kernel-module-xt-conntrack \
  kernel-module-xt-state \
"

B = "${WORKDIR}/build"

inherit update-rc.d systemd go-mod useradd

python __anonymous() {
    # Check your custom variable
    elevation_command = d.getVar('QBEE_ELEVATION_COMMAND') or ""
    exec_user = d.getVar('QBEE_EXEC_USER') or ""

    if exec_user and not elevation_command:
      bb.warn("QBEE_EXEC_USER is set to '%s' but QBEE_ELEVATION_COMMAND is not set. You must set QBEE_ELEVATION_COMMAND to a valid elevation command (e.g. 'sudo') if you want to run qbee-agent as a non-root user." % exec_user)

    if elevation_command and not exec_user:
      bb.warn("QBEE_ELEVATION_COMMAND is set to '%s' but QBEE_EXEC_USER is not set. You should set QBEE_EXEC_USER to a non-root user if you want to run qbee-agent as a non-root user." % elevation_command)

    # add sudo to RRECOMMENDS if the elevation command includes 'sudo' to allow users to use sudo for privileged operations without explicitly installing sudo in that case
    if 'sudo' in elevation_command:
      d.appendVar('RRECOMMENDS:%s' % d.getVar('PN'), ' sudo')

    if elevation_command:
      # check that we have a valid json array for the elevation command and check that the first element is an absolute path
      import json
      try:
        cmd_array = json.loads(elevation_command)
        if not cmd_array or not isinstance(cmd_array, list) or not cmd_array[0].startswith('/'):
          bb.fatal("QBEE_ELEVATION_COMMAND is set to '%s' but is not a valid JSON array with an absolute path as the first element. Please set QBEE_ELEVATION_COMMAND to a valid JSON array (e.g. '[\"/usr/bin/sudo\", \"-n\"]')" % elevation_command)
      except json.JSONDecodeError:
        bb.fatal("QBEE_ELEVATION_COMMAND is set to '%s' but is not a valid JSON array. Please set QBEE_ELEVATION_COMMAND to a valid JSON array (e.g. '[\"/usr/bin/sudo\", \"-n\"]')" % elevation_command)

    # Warn if auto-install is enabled, this is deprecated behavior that will be removed in a future release
    if d.getVar('QBEE_AUTO_INSTALL') == "1" and not d.getVar('QBEE_WARNING_SHOWN'):
        bb.warn("meta-qbee: Auto-appending qbee-agent to all images is deprecated. Please set QBEE_AUTO_INSTALL = '0' and add qbee-agent to IMAGE_INSTALL manually. This behavior will be removed in a future release.")
        d.setVar('QBEE_WARNING_SHOWN', '1')
}

# add useradd package and params if QBEE_EXEC_USER is set to create a non-root user for qbee-agent execution
# this needs to be defined here for the useradd class to pick it up, we cannot use a conditional inside the
# variable definition itself as that would not work with how useradd parses the variables
USERADD_PACKAGES += "${@ '${PN}' if d.getVar('QBEE_EXEC_USER') else ''}"
USERADD_PARAM:${PN} = "${@ '-M -s /sbin/nologin -d /nonexistent ' + d.getVar('QBEE_EXEC_USER') if d.getVar('QBEE_EXEC_USER') else ''}"

GO_IMPORT = "github.com/qbee-io/qbee-agent"
GO_EXTRA_LDFLAGS = "-w -s \
  -X go.qbee.io/agent/app.Version=${PV} \
  -X go.qbee.io/agent/app.Commit=${SRCREV}"

# Use static linking
GO_LINKSHARED = ""

UPSTREAM_CHECK_COMMITS = "1"

INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

SYSTEMD_SERVICE:${PN} = "qbee-agent.service"

INITSCRIPT_PACKAGES = "${PN}"
INITSCRIPT_NAME:${PN} = "qbee-agent"
INITSCRIPT_PARAMS:${PN} = "defaults 90 10"

FILES:${PN} += "${systemd_unitdir}/system/qbee-agent.service ${sysconfdir}/qbee/* ${prefix}/local/share/ca-certificates/qbee/*"
FILES:${PN} += "${@ '${sysconfdir}/systemd/system/${PN}.service.d/*' if d.getVar('QBEE_EXEC_USER') else ''}"

SRC_URI:append = "file://qbee-agent.service.unprivileged.in \
  file://qbee-agent.service.supplementary-groups.in \
  "

python __anonymous() {
  d.appendVarFlag('do_compile', 'network', '1') 
}

do_install:append () {
  # We need to rename the binary
  mv ${D}${bindir}/agent ${D}${bindir}/qbee-agent 

  install -m 0700 -d ${D}${sysconfdir}/qbee
  install -m 0700 -d ${D}${sysconfdir}/qbee/yocto
  install -m 0600 ${S}/src/${GO_IMPORT}/package/share/ssl/ca.cert ${D}${sysconfdir}/qbee/yocto

  sed -i -e 's#[@]QBEE_CONF_DIR[@]#${QBEE_CONF_DIR}#g' ${UNPACKDIR}/qbee-bootstrap-prep.sh.in
  sed -i -e 's#[@]QBEE_STATE_DIR[@]#${QBEE_STATE_DIR}#g' ${UNPACKDIR}/qbee-bootstrap-prep.sh.in
  sed -i -e 's#[@]QBEE_EXEC_USER[@]#${QBEE_EXEC_USER}#g' ${UNPACKDIR}/qbee-bootstrap-prep.sh.in

  install -m 0700 ${UNPACKDIR}/qbee-bootstrap-prep.sh.in ${D}/${sysconfdir}/qbee/yocto/qbee-bootstrap-prep.sh

  if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
    install -d ${D}/${systemd_unitdir}/system
    sed -e 's#[@]QBEE_CONF_DIR[@]#${QBEE_CONF_DIR}#g;s#[@]QBEE_STATE_DIR[@]#${QBEE_STATE_DIR}#g' \
      ${UNPACKDIR}/qbee-agent.service.in > ${UNPACKDIR}/qbee-agent.service
    sed -i -e 's#[@]QBEE_SYSTEMD_AFTER_TARGET[@]#${QBEE_SYSTEMD_AFTER_TARGET}#g' ${UNPACKDIR}/qbee-agent.service
    sed -i -e 's#[@]QBEE_SYSTEMD_WANTS_TARGET[@]#${QBEE_SYSTEMD_WANTS_TARGET}#g' ${UNPACKDIR}/qbee-agent.service
    install -m 0644 ${UNPACKDIR}/qbee-agent.service ${D}/${systemd_unitdir}/system/qbee-agent.service
  fi

  if ${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', 'true', 'false', d)}; then
    install -d ${D}${INIT_D_DIR}
    sed -e 's#[@]QBEE_CONF_DIR[@]#${QBEE_CONF_DIR}#g;s#[@]QBEE_STATE_DIR[@]#${QBEE_STATE_DIR}#g' \
      ${UNPACKDIR}/qbee-agent.init.in > ${UNPACKDIR}/qbee-agent.init
    install -m 0755 ${UNPACKDIR}/qbee-agent.init ${D}/${INIT_D_DIR}/qbee-agent
  fi

  if [ -n "${QBEE_BOOTSTRAP_KEY}" ]; then
    cat > ${B}/bootstrap-env << EOF
BOOTSTRAP_KEY=${QBEE_BOOTSTRAP_KEY}
DEVICE_NAME_TYPE=${QBEE_DEVICE_NAME_TYPE}
DEVICE_HUB_HOST=${QBEE_DEVICE_HUB_HOST}
CLEAN_SEEDING_INFO=${QBEE_CLEAN_SEEDING_INFO}
DISABLE_REMOTE_ACCESS=${QBEE_DISABLE_REMOTE_ACCESS}
TPM_DEVICE=${QBEE_TPM_DEVICE}
CA_CERT=${QBEE_CA_CERT}
ELEVATION_COMMAND='${QBEE_ELEVATION_COMMAND}'
EOF
  fi

  if [ -f ${B}/bootstrap-env ]; then
    install -m 0600 ${B}/bootstrap-env ${D}${sysconfdir}/qbee/yocto/.bootstrap-env
  fi

  # Add qbee backend CA's to system certs for 3rd party integrations
  install -m 0700 -d ${D}${prefix}/local/share/ca-certificates/qbee
  install -m 0600 ${S}/src/${GO_IMPORT}/package/share/ssl/ca.cert \
    ${D}${prefix}/local/share/ca-certificates/qbee/qbee-io-ca-certificates.crt

  # If QBEE_EXEC_USER is set, we add a systemd override.
  if [ -n "${QBEE_EXEC_USER}" ]; then
    # install systemd override for unprivileged execution
    install -m 0750 -d ${D}${sysconfdir}/systemd/system/${PN}.service.d
    sed -i -e 's#[@]QBEE_EXEC_USER[@]#${QBEE_EXEC_USER}#g' \
      ${UNPACKDIR}/qbee-agent.service.unprivileged.in
    install -m 0644 ${UNPACKDIR}/qbee-agent.service.unprivileged.in \
      ${D}${sysconfdir}/systemd/system/${PN}.service.d/10-unprivileged.conf
  fi

  # if both QBEE_SUPPLEMENTARY_GROUPS and QBEE_EXEC_USER are set, we add a systemd override to
  # add the execution user to the supplementary groups (eg. to allow docker socket access or tpm access)
  if [ -n "${QBEE_SUPPLEMENTARY_GROUPS}" ] && [ -n "${QBEE_EXEC_USER}" ]; then
    # install systemd override for supplementary groups
    install -m 0750 -d ${D}${sysconfdir}/systemd/system/${PN}.service.d
    sed -i -e 's#[@]QBEE_SUPPLEMENTARY_GROUPS[@]#${QBEE_SUPPLEMENTARY_GROUPS}#g' \
      ${UNPACKDIR}/qbee-agent.service.supplementary-groups.in
    install -m 0644 ${UNPACKDIR}/qbee-agent.service.supplementary-groups.in \
      ${D}${sysconfdir}/systemd/system/${PN}.service.d/20-supplementary-groups.conf
  fi
}

# Re-generate system certificates to include qbee CA so that third party systems
# can communicate with the device backend
pkg_postinst:${PN} () {
#!/bin/sh -e

$D$sbindir/update-ca-certificates --sysroot $D
}

