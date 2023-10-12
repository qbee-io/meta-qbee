require qbee-agent.inc

SRC_URI = "https://cdn.qbee.io/software/qbee-agent/${PV}/binaries/qbee-agent-${PV}.tar.gz \
  file://LICENSE \
  file://qbee-bootstrap-prep.sh.in \
  file://qbee-agent.service.in \
  file://qbee-agent.init.in \
  "

SRC_URI[sha256sum] = "35ac22b0822d4c33f97fc9301f4834038d0e421c6adc975393920b9e801834e8"
