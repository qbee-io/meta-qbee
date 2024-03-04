require qbee-agent.inc

SRC_URI = "https://cdn.qbee.io/software/qbee-agent/${PV}/binaries/qbee-agent-${PV}.tar.gz \
  file://qbee-bootstrap-prep.sh.in \
  file://qbee-agent.service.in \
  file://qbee-agent.init.in \
  "

SRC_URI[sha256sum] = "cd1462cb267b798b1afe214b5840da1fc2bcbdf4d5bf0b24c0bc7277c58e3b9c"

LICENSE="Apache-2.0"
LIC_FILES_CHKSUM ?= "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

RDEPENDS:${PN} += "coreutils bash kernel-modules sshd iptables ca-certificates os-release"
