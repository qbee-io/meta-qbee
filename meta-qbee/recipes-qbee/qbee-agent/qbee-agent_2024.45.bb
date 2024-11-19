require qbee-agent.inc

SRC_URI = "git://${GO_IMPORT};branch=master;protocol=https \
  file://qbee-bootstrap-prep.sh.in \
  file://qbee-agent.service.in \
  file://qbee-agent.init.in \
  "
SRCREV = "bdf5f11771964dbb69a0d8f1f93e69f75e5a83c5"

LICENSE="Apache-2.0"
LIC_FILES_CHKSUM ?= "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"
