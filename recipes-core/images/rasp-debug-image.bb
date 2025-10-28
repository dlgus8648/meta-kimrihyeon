SUMMARY = "Kernel Image"
DESCRIPTION ="asdf"
LICENSE = "CLOSED"
require recipes-extended/images/core-image-kernel-dev.bb



IMAGE_FSTYPES += " rpi-sdimg "


# 개발용 도구 추가
IMAGE_INSTALL:remove = "dropbear"
IMAGE_INSTALL:append = " \
    vim \
    nano \
    htop \
    make \
    gcc \
    bash \
    pahole \
    binutils \
    net-tools \
    iproute2 \
    openssh \
    linux-raspberrypi-devsrc \
    gawk \
    coreutils \
    gdb \
    kmod \
    strace \
    ltrace \
    git \
    python3 \
    bison \
    flex \
    perl \
    findutils \
    perl-modules \
    perl-dev \
    xz \
"
# 커널 빌드 트리와 devsrc 모두 준비
ROOTFS_POSTPROCESS_COMMAND += "prepare_full_kernel_env;"

prepare_full_kernel_env() {
    echo "[INFO] === Preparing full kernel build environment inside rootfs ==="

    install -d ${IMAGE_ROOTFS}/lib/modules/$(basename $(ls ${IMAGE_ROOTFS}/lib/modules))/
    
    KVER=$(basename $(ls ${IMAGE_ROOTFS}/lib/modules))
    MODULES_TGZ=$(ls ${DEPLOY_DIR_IMAGE}/modules-*raspberrypi4-64*.tgz | head -n 1)

    if [ -z "${MODULES_TGZ}" ]; then
        echo "[ERROR] Cannot find kernel modules package (*.tgz) under ${DEPLOY_DIR_IMAGE}"
        exit 1
    fi


    echo "[STEP 1] === Extracting kernel modules (.ko) into rootfs ==="
    install -d ${IMAGE_ROOTFS}/lib/modules/${KVER}
    tar -xzf ${MODULES_TGZ} -C ${IMAGE_ROOTFS}/lib/modules/${KVER}

    echo "[STEP 2] === Installing kernel headers for build ==="
    rm -rf ${IMAGE_ROOTFS}/lib/modules/${KVER}/build
    ln -sf /usr/src/kernel-devsrc ${IMAGE_ROOTFS}/lib/modules/${KVER}/build
    ln -sf /usr/src/kernel-devsrc ${IMAGE_ROOTFS}/lib/modules/${KVER}/source


    echo "[STEP 4] === Rebuilding kernel helper tools for ARM64 ==="
    if [ -x "${IMAGE_ROOTFS}/bin/bash" ]; then
        chroot ${IMAGE_ROOTFS} /bin/bash -c "
            cd /usr/src/kernel-devsrc && \
            make ARCH=arm64 prepare scripts
        "
    else
        echo "[WARN] bash not found inside rootfs — skipping prepare scripts"
    fi


    
}

DEPENDS += "rsync-native"

IMAGE_ROOTFS_EXTRA_SPACE = "16192000" 
# 개발 편의 기능 추가
EXTRA_IMAGE_FEATURES += " tools-sdk dev-pkgs debug-tweaks "
# RPI_KERNEL_IMAGETYPE = "Image-raspberrypi4-64.bin"
BOOT_SPACE = "165536"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"