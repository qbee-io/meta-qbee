# Private Go toolchain used only to build qbee-agent.
#
# This installs the official prebuilt Go distribution from go.dev into its own
# directory in the native sysroot. It deliberately does NOT provide go-native or
# virtual/*-go, so it never replaces the Go toolchain used by other recipes in
# the build. qbee-agent is built with CGO_ENABLED=0, so the upstream toolchain can
# cross-compile for every supported target on its own.
#
# Bump the version with: scripts/update-qbee-go.py

SUMMARY = "Go toolchain (upstream binary) used to build qbee-agent"
HOMEPAGE = "https://go.dev/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7998cb338f82d15c0eff93b7004d272a"

# Checksums available at https://go.dev/dl/?mode=json
SRC_URI = "https://dl.google.com/go/go${PV}.${BUILD_GOOS}-${BUILD_GOARCH}.tar.gz;name=go_${BUILD_GOTUPLE}"
SRC_URI[go_linux_amd64.sha256sum] = "7226736c2339719cda28fbf3a54d71aa63bcefe376e98cef00034268dab24840"
SRC_URI[go_linux_arm64.sha256sum] = "3392582988a273a87ab5043ee1b2c9c17a2c3c00580bf9326c47a2e0aaaeac92"

UPSTREAM_CHECK_URI = "https://go.dev/dl/"
UPSTREAM_CHECK_REGEX = "go(?P<pver>\d+(\.\d+)+)\.linux"

CVE_PRODUCT = "golang:go"
CVE_STATUS[CVE-2024-3566] = "not-applicable-platform: Issue only applies on Windows"
CVE_STATUS[CVE-2024-24786] = "cpe-incorrect: this CVE is for golang-google-protobuf"

S = "${WORKDIR}/go"

inherit goarch native

# Location of the toolchain inside the native sysroot; qbee-agent.inc uses the same path.
QBEE_GOROOT_SUBDIR = "qbee-go"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
	find ${S} -depth -type d -name testdata -exec rm -rf {} +

	install -d ${D}${libdir}/${QBEE_GOROOT_SUBDIR}
	cp --preserve=mode,timestamps -R ${S}/. ${D}${libdir}/${QBEE_GOROOT_SUBDIR}/
}
