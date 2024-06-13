require qbee-agent.inc

SRC_URI = "git://${GO_IMPORT};branch=main;protocol=https \
  file://qbee-bootstrap-prep.sh.in \
  file://qbee-agent.service.in \
  file://qbee-agent.init.in \
  "
SRCREV = "3219c17730eab4c80598494732b4bed4c4c18b51"

LICENSE="Apache-2.0"
LIC_FILES_CHKSUM ?= "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"
