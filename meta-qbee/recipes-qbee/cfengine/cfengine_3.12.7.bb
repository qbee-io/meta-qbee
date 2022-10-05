SUMMARY = "CFEngine is an IT infrastructure automation framework"

DESCRIPTION = "CFEngine is an IT infrastructure automation framework \
that helps engineers, system administrators and other stakeholders \
in an IT system to manage and understand IT infrastructure throughout \
its lifecycle. CFEngine takes systems from Build to Deploy, Manage and Audit."

HOMEPAGE = "http://cfengine.com"

LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://LICENSE;md5=f8b34828ab373d6b1bb4b0fc60a78494"

DEPENDS = "attr lmdb libpcre openssl curl pkgconfig"

SRC_URI = "https://cfengine-package-repos.s3.amazonaws.com/tarballs/${BP}.tar.gz \
           file://set-path-of-default-config-file.patch \
           file://processes_select_patch.patch \
           file://patch_for_openssl_3.patch \
           "

SRC_URI[md5sum] = "e78245048aafe1a69c2822758394990f"
SRC_URI[sha256sum] = "df98bc1260d6ac1f7b653ffa738dd5db6273fd59c182750a0cb1a895d6669ddb"

inherit autotools

cf_workdir = "/var/lib/qbee"
cf_statedir = "/run/qbee/state"
cf_logdir = "${workdir}/log"
cf_piddir = "${workdir}/run"

export EXPLICIT_VERSION="${PV}"

EXTRA_OECONF = "hw_cv_func_va_copy=yes --with-piddir=${cf_piddir} --with-workdir=${cf_workdir} --with-logdir=${cf_logdir} --with-statedir=${cf_statedir}"
EXTRA_OECONF += "--with-pcre --with-openssl --with-lmdb --with-libcurl --disable-libtool-lock"
EXTRA_OECONF += "--without-libvirt --without-pam  --without-libxml2 --without-postgresql --without-libacl --without-mysql --without-tokyocabinet --without-libyaml"
#EXTRA_OEMAKE += "'LDFLAGS=${LDFLAGS} -Wl,-rpath="${prefix}/lib -L${prefix}/lib'"
#TARGET_CFLAGS += "-D__BUSYBOX__"

do_compile:prepend() {
   echo "                                   Workdir ${WORKDIR}"
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

do_configure:append() {
  rm -rf ${S}/cf-monitord/Makefile
  rm -rf ${B}/cf-monitord/Makefile
  cat > ${S}/cf-monitord/Makefile << EOF
all:
.PHONY: all
install:
.PHONY: install
EOF
  cp ${S}/cf-monitord/Makefile ${B}/cf-monitord
}

