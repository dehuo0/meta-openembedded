SUMMARY = "A full-featured SSL VPN solution via tun device."
HOMEPAGE = "http://openvpn.sourceforge.net"
SECTION = "net"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=5aac200199fde47501876cba7263cb0c"
DEPENDS = "lzo openssl iproute2 ${@base_contains('DISTRO_FEATURES', 'pam', 'libpam', '', d)}"

inherit autotools systemd

SRC_URI = "http://swupdate.openvpn.org/community/releases/openvpn-${PV}.tar.gz \
           file://openvpn \
           file://openvpn@.service \
           file://openvpn-volatile.conf \
           file://CVE-2017-7508.patch \
           file://CVE-2017-7520.patch \
           file://CVE-2017-7521.patch \
           file://CVE-2017-7521-2.patch \
           file://CVE-2016-6329.patch \
           file://CVE-2017-7479.patch \
"

SRC_URI[md5sum] = "070bca95e478f88dff9ec6a221e2c3f7"
SRC_URI[sha256sum] = "1f02a4cd6aeb6250ca9311560875b10ce8957a3c9101a8005bd1e17e5b03146e"

SYSTEMD_SERVICE_${PN} += "openvpn@loopback-server.service openvpn@loopback-client.service"
SYSTEMD_AUTO_ENABLE = "disable"

CFLAGS += "-fno-inline"

# I want openvpn to be able to read password from file (hrw)
EXTRA_OECONF += "--enable-password-save --enable-iproute2"
EXTRA_OECONF += "${@base_contains('DISTRO_FEATURES', 'pam', '', '--disable-plugin-auth-pam', d)}"

# Explicitly specify IPROUTE to bypass the configure-time check for /sbin/ip on the host.
EXTRA_OECONF += "IPROUTE=/sbin/ip"

do_install_append() {
    install -d ${D}/${sysconfdir}/init.d
    install -m 755 ${WORKDIR}/openvpn ${D}/${sysconfdir}/init.d

    install -d ${D}/${sysconfdir}/openvpn
    install -d ${D}/${sysconfdir}/openvpn/sample
    install -m 755 ${S}/sample/sample-config-files/loopback-server  ${D}${sysconfdir}/openvpn/sample/loopback-server.conf
    install -m 755 ${S}/sample/sample-config-files/loopback-client  ${D}${sysconfdir}/openvpn/sample/loopback-client.conf
    install -dm 755 ${D}${sysconfdir}/openvpn/sample/sample-keys
    install -m 644 ${S}/sample/sample-keys/* ${D}${sysconfdir}/openvpn/sample/sample-keys

    if ${@base_contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        install -d ${D}/${systemd_unitdir}/system
        install -m 644 ${WORKDIR}/openvpn@.service ${D}/${systemd_unitdir}/system
        install -m 644 ${WORKDIR}/openvpn@.service ${D}/${systemd_unitdir}/system/openvpn@loopback-server.service
        install -m 644 ${WORKDIR}/openvpn@.service ${D}/${systemd_unitdir}/system/openvpn@loopback-client.service

        install -d ${D}/${localstatedir}
        install -d ${D}/${localstatedir}/lib
        install -d -m 710 ${D}/${localstatedir}/lib/openvpn
        install -d -m 755 ${D}/${localstatedir}/run/
        install -d -m 755 ${D}/${localstatedir}/run/openvpn

        install -d ${D}${sysconfdir}/tmpfiles.d
        install -m 0644 ${WORKDIR}/openvpn-volatile.conf ${D}${sysconfdir}/tmpfiles.d/openvpn.conf
    fi
}

PACKAGES =+ " ${PN}-sample "

RRECOMMENDS_${PN} = "kernel-module-tun"

FILES_${PN}-dbg += "${libdir}/openvpn/plugins/.debug"
FILES_${PN} += "${systemd_unitdir}/system/openvpn@.service \
                /run"
FILES_${PN}-sample += "${systemd_unitdir}/system/openvpn@loopback-server.service \
                       ${systemd_unitdir}/system/openvpn@loopback-client.service \
                       ${sysconfdir}/openvpn/sample/"