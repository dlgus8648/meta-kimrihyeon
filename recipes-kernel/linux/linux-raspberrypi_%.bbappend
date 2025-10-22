FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "file://kim_gdb-fragment.cfg"
KERNEL_CONFIG_FRAGMENTS += "kim_gdb-fragment.cfg"
KBUILD_CFLAGS += "-fdebug-prefix-map=${S}=/usr/src/kernel"
