require qbee-agent.inc

SRC_URI = "https://cdn.qbee.io/software/qbee-agent/${PV}/binaries/qbee-agent-${PV}.tar.gz \
  file://LICENSE \
  file://qbee-bootstrap-prep.sh.in \
  file://qbee-agent.service.in \
  file://qbee-agent.init.in \
  "

SRC_URI[sha256sum] = "4452178fa35439c949a96e089b21e919f744a39b47e13eaceedbf4c7665ea49c"
