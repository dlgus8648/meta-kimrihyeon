FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += " \
    file://kim_gdb-fragment.cfg \
"
KERNEL_CONFIG_FRAGMENTS += "kim_gdb-fragment.cfg"

DEPENDS += "rsync-native"
DEPENDS += "qemu-native"

# --- spec 파일 생성 전에, 경로 교정 ---
do_install:prepend() {
    echo "[FIX] Normalizing /bin path in all kernel helper scripts..."

    # 커널 빌드 트리 내 모든 shebang 정정
    find ${S} -type f -exec sed -i 's|^#! */bin/env|#!/usr/bin/env|g' {} +
    find ${S} -type f -exec sed -i 's|^#! */bin/awk|#!/usr/bin/awk|g' {} +
}
KBUILD_CFLAGS += "-fdebug-prefix-map=${S}=/usr/src/kernel"
do_install:append() {
    echo "[INFO] Copying full kernel source into /usr/src/kernel-devsrc (preserving symlinks)..."
    install -d ${D}/usr/src/kernel-devsrc
    rsync -a --links --exclude=".git" \
          --owner --group --chown=0:0 \
          ${S}/ ${D}/usr/src/kernel-devsrc/
    install -m 0644 ${B}/.config ${D}/usr/src/kernel-devsrc/.config || true
    install -m 0644 ${B}/System.map ${D}/usr/src/kernel-devsrc/System.map || true
    install -m 0644 ${B}/Module.symvers ${D}/usr/src/kernel-devsrc/Module.symvers || true

}



PACKAGES += "${PN}-devsrc"
FILES:${PN}-devsrc += "/usr/src/kernel-devsrc"
DESCRIPTION:${PN}-devsrc = "Full kernel source for out-of-tree module builds"