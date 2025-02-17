PACKAGECONFIG = "static \
                   upx \
                   ${@bb.utils.contains('DISTRO_FEATURES', 'seccomp', 'seccomp', '', d)} \
                   ${@bb.utils.contains('DISTRO_FEATURES', 'selinux', 'selinux', '', d)} \
	          "

PACKAGECONFIG[upx] = ",,upx-native"

do_compile:append() {
    upx -9 ${S}/src/import/runc
}

INHIBIT_PACKAGE_STRIP = "1" 
INSANE_SKIP:${PN} += "ldflags already-stripped textrel"