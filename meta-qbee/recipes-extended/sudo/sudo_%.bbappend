FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
SRC_URI += "file://qbee-agent.sudoers.in"

do_install:append() {
  # if both QBEE_EXEC_USER and QBEE_ELEVATION_COMMAND are set, we add a sudoers file to allow the
  # execution user to run privileged operations via the elevation command
  if [ -n "${QBEE_ELEVATION_COMMAND}" ] && [ -n "${QBEE_EXEC_USER}" ]; then
    # install sudoers file for qbee-agent
    install -m 0750 -d "${D}${sysconfdir}/sudoers.d"
    sed -i -e "s#[@]QBEE_EXEC_USER[@]#${QBEE_EXEC_USER}#g" \
      "${WORKDIR}/qbee-agent.sudoers.in"
    install -m 0600 "${WORKDIR}/qbee-agent.sudoers.in" "${D}${sysconfdir}/sudoers.d/qbee-agent"
  fi
}