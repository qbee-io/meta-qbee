require qbee-agent.inc

SRC_URI = "https://cdn.qbee.io/software/qbee-agent/${PV}/binaries/qbee-agent-${PV}.tar.gz \
  file://LICENSE \
  file://qbee-bootstrap-prep.sh \ 
  file://qbee-agent.service \ 
  file://qbee-agent.init \ 
  "

SRC_URI[sha256sum] = "edcf319c4ce17e9844df598fa796aa87303a1bf238299dbeeb41c94ff5de5e1d"
