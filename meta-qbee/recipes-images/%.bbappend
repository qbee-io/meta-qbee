# Default to 1 to maintain backward compatibility for existing users
QBEE_AUTO_INSTALL ?= "1"

# Only append if the toggle is set to 1
IMAGE_INSTALL:append = "${@' qbee-agent' if d.getVar('QBEE_AUTO_INSTALL') == '1' else ''}"