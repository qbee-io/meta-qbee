PACKAGECONFIG += " upx"
PACKAGECONFIG[upx] = ",,upx-native"

do_compile:append() {
    upx -9 ${S}/bin/containerd
    upx -9 ${S}/bin/containerd-shim-runc-v2
    upx -9 ${S}/bin/ctr
}

# Copied from k3s
INHIBIT_PACKAGE_STRIP = "1" 
INSANE_SKIP:${PN} += "ldflags already-stripped textrel"