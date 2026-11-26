# qbee-go.bbclass
#
# Builds a statically linked, non-cgo Go program with the private qbee-go-native
# toolchain. It replaces go.bbclass/go-mod.bbclass for qbee recipes, so the Go
# version qbee needs can be pinned without replacing the distro Go toolchain that
# other recipes use.
#
# The recipe must set:
#   GO_IMPORT            import path, also used for the source layout
# and may set:
#   GO_EXTRA_LDFLAGS     extra -ldflags (e.g. -X version stamping)
#   QBEE_GO_PACKAGE      package to build, relative to the module root (default ".")
#   QBEE_GO_BINARY       output path of the built binary (default ${B}/${BPN}),
#                        installed into ${bindir} by the default do_install
#   QBEE_GOBUILDFLAGS    flags passed to `go build`
#
# Fetch the sources into the GOPATH-style layout with:
#   SRC_URI = "git://${GO_IMPORT};...;destsuffix=${GO_SRCURI_DESTSUFFIX}"

inherit goarch

GO_IMPORT ??= "${@bb.fatal("The recipe needs to set GO_IMPORT for qbee-go.bbclass to work")}"

DEPENDS += "qbee-go-native"

# Toolchain location, matches the install path of qbee-go-native
QBEE_GOROOT = "${STAGING_LIBDIR_NATIVE}/qbee-go"
QBEE_GO = "${QBEE_GOROOT}/bin/go"

# Sources live at ${S}/src/${GO_IMPORT}
GO_SRCURI_DESTSUFFIX = "${@os.path.join(os.path.basename(d.getVar('S')), 'src', d.getVar('GO_IMPORT')) + '/'}"
QBEE_GO_SRCDIR = "${S}/src/${GO_IMPORT}"

GO_EXTRA_LDFLAGS ?= ""
QBEE_GO_PACKAGE ?= "."
QBEE_GO_BINARY ?= "${B}/${BPN}"
QBEE_GO_PARALLEL_BUILD ?= "${@oe.utils.parallel_make_argument(d, '-p %d')}"
# -trimpath keeps build paths out of the binary (buildpaths QA)
QBEE_GOBUILDFLAGS ?= "-v -trimpath -buildvcs=false -modcacherw ${QBEE_GO_PARALLEL_BUILD}"

# Go environment. The variables here are Go's own interface, so they follow the
# qbee-go-native version rather than the Yocto release.
export GOROOT = "${QBEE_GOROOT}"
export GOOS = "${TARGET_GOOS}"
export GOARCH = "${TARGET_GOARCH}"
export GOHOSTOS = "${BUILD_GOOS}"
export GOHOSTARCH = "${BUILD_GOARCH}"
export CGO_ENABLED = "0"
# Never let the go command download a different toolchain than qbee-go-native
export GOTOOLCHAIN = "local"
export GOENV = "off"
export GOWORK = "off"
export GOFLAGS = ""
export GOPROXY ??= "https://proxy.golang.org,direct"
export GOPATH = "${B}"
export GOCACHE = "${B}/.cache"
export GOMODCACHE = "${B}/pkg/mod"
export GOTMPDIR = "${WORKDIR}/tmp-go-build"
GOTMPDIR[vardepvalue] = ""

do_configure[noexec] = "1"

do_compile() {
	export TMPDIR="${GOTMPDIR}"
	# The go command writes telemetry counters below $HOME; keep that inside WORKDIR
	export HOME="${WORKDIR}/go-home"
	# Sub-architecture settings; goarch.bbclass leaves these empty for other arches
	if [ -n "${TARGET_GOARM}" ]; then export GOARM="${TARGET_GOARM}"; fi
	if [ -n "${TARGET_GO386}" ]; then export GO386="${TARGET_GO386}"; fi
	if [ -n "${TARGET_GOMIPS}" ]; then export GOMIPS="${TARGET_GOMIPS}"; fi

	cd ${QBEE_GO_SRCDIR}
	${QBEE_GO} build ${QBEE_GOBUILDFLAGS} -ldflags "${GO_EXTRA_LDFLAGS}" \
		-o ${QBEE_GO_BINARY} ${QBEE_GO_PACKAGE}
}
do_compile[dirs] = "${B} ${GOTMPDIR} ${WORKDIR}/go-home"
do_compile[postfuncs] += "qbee_go_verify_binary"

# Check the build info embedded in the binary: it must have been built by the
# qbee-go-native toolchain (not a host Go or an auto-downloaded toolchain),
# with cgo disabled and for the target architecture.
qbee_go_verify_binary() {
	expected="$(head -n1 ${QBEE_GOROOT}/VERSION)"
	info="$(${QBEE_GO} version -m ${QBEE_GO_BINARY})" || \
		bbfatal "Could not read Go build info from ${QBEE_GO_BINARY}"

	actual="$(echo "$info" | awk 'NR==1 {print $2}')"
	if [ "$actual" != "$expected" ]; then
		bbfatal "${QBEE_GO_BINARY} was built with $actual, expected $expected from qbee-go-native"
	fi

	cgo="$(echo "$info" | awk '$1 == "build" && $2 ~ /^CGO_ENABLED=/ {sub(/^CGO_ENABLED=/, "", $2); print $2}')"
	if [ "$cgo" != "0" ]; then
		bbfatal "${QBEE_GO_BINARY} was built with CGO_ENABLED=$cgo, expected 0"
	fi

	goarch="$(echo "$info" | awk '$1 == "build" && $2 ~ /^GOARCH=/ {sub(/^GOARCH=/, "", $2); print $2}')"
	if [ "$goarch" != "${TARGET_GOARCH}" ]; then
		bbfatal "${QBEE_GO_BINARY} was built for GOARCH=$goarch, expected ${TARGET_GOARCH}"
	fi

	bbnote "${QBEE_GO_BINARY}: $actual, CGO_ENABLED=$cgo, GOARCH=$goarch"
}
# Go modules are downloaded during compile
do_compile[network] = "1"

do_install() {
	install -d ${D}${bindir}
	install -m 0755 ${QBEE_GO_BINARY} ${D}${bindir}/
}

SPDX_PACKAGE_URLS =+ "pkg:golang/${GO_IMPORT}@${PV} "

# Go sets its own linker flags
INSANE_SKIP:${PN} += "ldflags"
