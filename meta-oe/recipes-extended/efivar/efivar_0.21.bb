SUMMARY = "Tools to manipulate UEFI variables"
DESCRIPTION = "efivar provides a simple command line interface to the UEFI variable facility"
HOMEPAGE = "https://github.com/rhinstaller/efivar"

LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=6626bb1e20189cfa95f2c508ba286393"

DEPENDS = "popt"
DEPENDS_append_class-target = " efivar-native"

SRCREV = "aab6c2a64d90b6e5a63661fb5bd6be8d878b0784"
SRC_URI = "git://github.com/rhinstaller/efivar.git"
SRC_URI_append_class-target = " file://0001-efivar-fix-for-cross-compile.patch"
SRC_URI_append_class-native = " file://efivar-drop-options-not-supported-by-lower-version-gcc.patch"

S = "${WORKDIR}/git"

do_install() {
    oe_runmake install DESTDIR=${D}
}

do_compile_class-native() {
    oe_runmake -C src makeguids
}

do_install_class-native() {
    install -D -m 0755 ${B}/src/makeguids ${D}${bindir}/makeguids
}

BBCLASSEXTEND = "native"
