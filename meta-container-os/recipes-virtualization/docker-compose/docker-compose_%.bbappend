PACKAGECONFIG = "docker-plugin upx"
PACKAGECONFIG[upx] = ",,upx-native"

do_compile:append() {
    upx -9 ${S}/src/import/bin/docker-compose
}

INHIBIT_PACKAGE_STRIP = "1" 
INSANE_SKIP:${PN} += "ldflags already-stripped textrel"