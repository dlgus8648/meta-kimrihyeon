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
    install -d ${IMAGE_ROOTFS}/lib/modules/${KVER}/build
    rsync -a --exclude 'scripts/basic/fixdep' \
              --exclude 'scripts/mod/modpost' \
              ${TMPDIR}/work-shared/${MACHINE}/kernel-build-artifacts/ \
              ${IMAGE_ROOTFS}/lib/modules/${KVER}/build/





    KERNEL_VER=$(basename $(ls ${IMAGE_ROOTFS}/lib/modules))
    KERNEL_BUILD_PATH=${IMAGE_ROOTFS}/lib/modules/${KERNEL_VER}/build
    KERNEL_SRC_PATH=${IMAGE_ROOTFS}/usr/src/kernel-devsrc
    KERNEL_STAGING=${STAGING_KERNEL_BUILDDIR}

    # 1. 전체 커널 빌드 트리를 /lib/modules/.../build 에 복사
    echo "[STEP 1] Copying full kernel build tree to /lib/modules/${KERNEL_VER}/build ..."
    install -d ${KERNEL_BUILD_PATH}
    cp -r ${KERNEL_STAGING}/* ${KERNEL_BUILD_PATH}/ || true

    # 2. devsrc에도 동일 빌드 산출물 복사
    if [ -d "${KERNEL_SRC_PATH}" ]; then
        echo "[STEP 2] Copying kernel build artifacts into /usr/src/kernel-devsrc ..."
        cp -f ${KERNEL_STAGING}/.config ${KERNEL_SRC_PATH}/ || true
        cp -f ${KERNEL_STAGING}/Module.symvers ${KERNEL_SRC_PATH}/ || true
        cp -rf ${KERNEL_STAGING}/include/generated ${KERNEL_SRC_PATH}/include/ || true
        cp -rf ${KERNEL_STAGING}/include/config ${KERNEL_SRC_PATH}/include/ || true
        ln -sf /usr/src/kernel-devsrc ${IMAGE_ROOTFS}/lib/modules/${KERNEL_VER}/source
    else
        echo "[WARN] kernel-devsrc not found under /usr/src"
    fi

    echo "[STEP 3] Kernel build environment ready."
}

DEPENDS += "rsync-native"

IMAGE_ROOTFS_EXTRA_SPACE = "16192000" 
# 개발 편의 기능 추가
EXTRA_IMAGE_FEATURES += " tools-sdk dev-pkgs debug-tweaks "
# RPI_KERNEL_IMAGETYPE = "Image-raspberrypi4-64.bin"
BOOT_SPACE = "165536"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

