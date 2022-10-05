SUMMARY = "CFEngine is an IT infrastructure automation framework"

DESCRIPTION = "CFEngine is an IT infrastructure automation framework \
that helps engineers, system administrators and other stakeholders \
in an IT system to manage and understand IT infrastructure throughout \
its lifecycle. CFEngine takes systems from Build to Deploy, Manage and Audit."

HOMEPAGE = "http://cfengine.com"

LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://LICENSE;md5=f8b34828ab373d6b1bb4b0fc60a78494"

DEPENDS = "attr lmdb libpcre openssl curl"

SRC_URI = "https://cfengine-package-repos.s3.amazonaws.com/tarballs/${BP}.tar.gz \
           file://set-path-of-default-config-file.patch \
           file://processes_select_patch.patch \
           "

SRC_URI[md5sum] = "d8b60b3a9d6a3ec7c0b6c228cf29b562"
SRC_URI[sha256sum] = "245a98b18e075fbd2e9a460c0c25b31fb6807d9da5f4b2ec1fc571ed7267a9aa"

inherit autotools

cf_workdir = "/var/lib/qbee"
cf_statedir = "/run/qbee/state"
cf_logdir = "${workdir}/log"
cf_piddir = "${workdir}/run"

export EXPLICIT_VERSION="${PV}"

EXTRA_OECONF = "hw_cv_func_va_copy=yes --with-piddir=${cf_piddir} --with-workdir=${cf_workdir} --with-logdir=${cf_logdir} --with-statedir=${cf_statedir}"
EXTRA_OECONF += "--with-pcre --with-openssl --with-lmdb --with-libcurl"
EXTRA_OECONF += "--without-libvirt --without-pam  --without-libxml2 --without-postgresql --without-libacl --without-mysql --without-tokyocabinet --without-libyaml"
#EXTRA_OEMAKE += "'LDFLAGS=${LDFLAGS} -Wl,-rpath=${prefix}/lib -L${prefix}/lib'"
#TARGET_CFLAGS += "-D__BUSYBOX__"

do_compile:prepend() {
   echo "                                   Werkdir ${WORKDIR}"
   echo "                                   Compiler ${CC}"
   echo "                                   BUILD_LDFLAGS ${BUILD_LDFLAGS}"
   echo "                                   LDFLAGS ${LDFLAGS}"
   echo "                                   TARGET_LDFLAGS ${TARGET_LDFLAGS}"
}

do_install:append() {
    install -d ${D}${cf_workdir}/bin
    for f in `ls ${D}${bindir}`; do
        ln -s ${bindir}/`basename $f` ${D}${cf_workdir}/bin/
    done

    # Prevent cfengine native service files
    #rm -rf ${D}/usr/lib/systemd
}
