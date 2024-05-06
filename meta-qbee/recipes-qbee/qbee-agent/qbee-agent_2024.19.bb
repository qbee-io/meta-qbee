require qbee-agent.inc

SRC_URI = "git://${GO_IMPORT};branch=main;protocol=https \
  file://qbee-bootstrap-prep.sh.in \
  file://qbee-agent.service.in \
  file://qbee-agent.init.in \
  "
SRCREV = "9d0ed1bc1bab18c75a73dbacc2026e86113c97aa"

LICENSE="Apache-2.0"
LIC_FILES_CHKSUM ?= "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"
