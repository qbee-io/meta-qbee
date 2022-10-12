DESCRIPTION = "This is a simple example recipe that cross-compiles a Go program."
HOMEPAGE = "https://qbee.io/"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

GO_IMPORT = "github.com/qbee-io/qbee-agent"
GO_INSTALL = "${GO_IMPORT}/cmd/qbee-agent"

SRC_URI = "git://git@${GO_IMPORT};branch=master;protocol=ssh"
SRCREV = "0de6c31074c57c7c2482a6c4a8a46fc31b4b7106"
UPSTREAM_CHECK_COMMITS = "1"

inherit go-mod

python __anonymous() {
  d.appendVarFlag('do_compile', 'network', '1') 
}

# This is just to make clear where this example is
#do_compile() {
#  make yocto
#}

do_install:append() {
    cp ${D}${bindir}/qbee-agent ${D}${bindir}/${BPN}
}

