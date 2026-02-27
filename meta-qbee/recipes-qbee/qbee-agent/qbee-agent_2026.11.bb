require qbee-agent.inc

SRC_URI = "git://${GO_IMPORT};branch=NOJIRA-privileged-command-reboot;protocol=https \
  file://qbee-bootstrap-prep.sh.in \
  file://qbee-agent.service.in \
  file://qbee-agent.init.in \
  "
SRCREV = "edf39c401f118ea4a96a900bb221d37bda94f844"

LICENSE="Apache-2.0"
LIC_FILES_CHKSUM ?= "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"
