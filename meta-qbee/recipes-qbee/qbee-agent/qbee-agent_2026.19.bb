require qbee-agent.inc

SRC_URI = "git://${GO_IMPORT};branch=main;protocol=https;destsuffix=${GO_SRCURI_DESTSUFFIX} \
  file://qbee-bootstrap-prep.sh.in \
  file://qbee-agent.service.in \
  file://qbee-agent.init.in \
  "
SRCREV = "99a0b5a56bb0ae2d7dec29abcf25d29be16f2c36"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM ?= "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"
