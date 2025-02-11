do_install:append() {

  install -d ${D}${sysconfdir}/docker

  echo '{"data-root":"/data/docker"}' > ${D}${sysconfdir}/docker/daemon.json
}
