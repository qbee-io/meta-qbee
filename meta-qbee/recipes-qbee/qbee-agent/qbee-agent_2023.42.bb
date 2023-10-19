require qbee-agent.inc

SRC_URI = "https://cdn.qbee.io/software/qbee-agent/${PV}/binaries/qbee-agent-${PV}.tar.gz \
  file://LICENSE \
  file://qbee-bootstrap-prep.sh.in \
  file://qbee-agent.service.in \
  file://qbee-agent.init.in \
  "

SRC_URI[sha256sum] = "2f490b1dc1b207f5cac031ac05d65a9edd312587d99ca3ce775dca772b946bf5"
