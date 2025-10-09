SUMMARY = "${PN} file recpipe"
LICENSE = "CLOSED"

SRC_URI = "file://mysource" 
S = "${WORKDIR}/mysource" 

# in package helloworldkim doesn't have GNU_HASH 
TARGET_CC_ARCH += "${LDFLAGS}" 

do_compile(){
    ${CC} ${S}/hellokim.c -o ${B}/hellokim
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/hellokim ${D}${bindir}
}
