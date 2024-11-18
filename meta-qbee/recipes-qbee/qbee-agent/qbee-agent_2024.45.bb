require qbee-agent.inc

SRC_URI = "git://${GO_IMPORT};branch=NOJIRA-fix-params-os-release;protocol=https \
  file://qbee-bootstrap-prep.sh.in \
  file://qbee-agent.service.in \
  file://qbee-agent.init.in \
  "
SRCREV = "a28229b711f91e49d31bee4086552ae47aa31f2f"

LICENSE="Apache-2.0"
LIC_FILES_CHKSUM ?= "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"
