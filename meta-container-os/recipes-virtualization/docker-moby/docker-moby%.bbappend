PACKAGECONFIG += " upx"
PACKAGECONFIG[upx] = ",,upx-native"

do_compile:append() {
  upx -9 ${WORKDIR}/git/cli/build/docker-linux*
  upx -9 ${S}/src/import/bundles/dynbinary-daemon/dockerd
  upx -9 ${WORKDIR}/git/libnetwork/bin/docker-proxy*
}

do_install:append() {

  install -d ${D}${sysconfdir}/docker

  echo '{"data-root":"/data/docker"}' > ${D}${sysconfdir}/docker/daemon.json
}
