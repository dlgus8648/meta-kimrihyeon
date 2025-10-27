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
    bc \
    perl-dev \
    xz \
"
# 커널 빌드 트리와 devsrc 모두 준비
ROOTFS_POSTPROCESS_COMMAND += "prepare_full_kernel_env;"

prepare_full_kernel_env() {
    
    KVER=$(basename $(ls ${IMAGE_ROOTFS}/lib/modules))
    MODULES_TGZ=$(ls ${DEPLOY_DIR_IMAGE}/modules-*raspberrypi4-64*.tgz | head -n 1)

    if [ -z "${MODULES_TGZ}" ]; then
        echo "[ERROR] Cannot find kernel modules package (*.tgz) under ${DEPLOY_DIR_IMAGE}"
        exit 1
    fi

    echo "[STEP 1] === Extracting kernel modules (.ko) into rootfs ==="
    install -d ${IMAGE_ROOTFS}/lib/modules/${KVER}
    tar -xzf ${MODULES_TGZ} -C ${IMAGE_ROOTFS}/lib/modules/${KVER}

    echo "[STEP 2] === Copying FULL kernel build tree ==="


    # 실제 Yocto 커널 빌드 디렉토리
    REAL_SRC=${TMPDIR}/work/raspberrypi4_64-poky-linux/linux-raspberrypi/6.6.63+git/linux-raspberrypi4_64-standard-build
    TARGET_BUILD=${IMAGE_ROOTFS}/lib/modules/${KVER}/build
    BUILD_ARTIFACTS=${TMPDIR}/work-shared/${MACHINE}/kernel-build-artifacts

    if [ ! -d "${REAL_SRC}" ]; then
        echo "[ERROR] Missing kernel build source: ${REAL_SRC}"
        exit 1
    fi

    # install -d ${TARGET_BUILD}

    echo "[COPY] Copying full kernel source from:"
    echo "       ${REAL_SRC} → ${TARGET_BUILD}"

    cp -a ${REAL_SRC}/* ${TARGET_BUILD}/
   
    KIM_TOOLCHAIN=${TMPDIR}/kim_aarch64-toolchain
    mkdir -p $KIM_TOOLCHAIN
    # gcc 관련
    cp -a ${TMPDIR}/sysroots-components/x86_64/gcc-cross-aarch64/usr/bin/aarch64-poky-linux/* $KIM_TOOLCHAIN/
    # binutils 관련
    cp -a ${TMPDIR}/sysroots-components/x86_64/binutils-cross-aarch64/usr/bin/aarch64-poky-linux/* $KIM_TOOLCHAIN/

    echo "[DEBUG] TOOLCHAIN = ${KIM_TOOLCHAIN}"
    ls ${KIM_TOOLCHAIN}/aarch64-poky-linux-gcc || echo "gcc not found!"
    echo "[DEBUG] CC before unset: $CC"
    echo "[DEBUG] HOSTCC before unset: $HOSTCC"
    #unset CC
    #unset CXX
    #export HOSTCC=/usr/bin/gcc
    #export HOSTCXX=/usr/bin/g++
    #export HOSTLD=/usr/bin/ld
    #export HOSTCFLAGS="-O2 -Wall"
    #echo "[DEBUG] CC after unset: $CC"
    #echo "[DEBUG] HOSTCC after unset: $HOSTCC"
    #echo "HOSTCC = $HOSTCC"
    #echo "HOSTCXX = $HOSTCXX"
    #echo "HOSTLD = $HOSTLD"
    #echo "HOSTCFLAGS = $HOSTCFLAGS"
    SDK_PATH=/opt/poky/5.0.12
    CROSS_COMPILE=${SDK_PATH}/sysroots/x86_64-pokysdk-linux/usr/bin/aarch64-poky-linux/aarch64-poky-linux-
    sed -i 's@scripts/atomic/check-atomics.sh@true@g' ${TARGET_BUILD}/Makefile
    make -C ${TARGET_BUILD} ARCH=arm64 CROSS_COMPILE=${CROSS_COMPILE} V=1




    echo "[SYNC] Syncing kernel build artifacts (Module.symvers, .config, include/generated)"
    # rsync -a ${BUILD_ARTIFACTS}/ ${TARGET_BUILD}/

    echo "[DONE] === Full kernel source + build tree installed at /lib/modules/${KVER}/build ==="

}

DEPENDS += "rsync-native bison-native flex-native bc-native coreutils-native"

IMAGE_ROOTFS_EXTRA_SPACE = "16192000" 
# 개발 편의 기능 추가
EXTRA_IMAGE_FEATURES += " tools-sdk dev-pkgs debug-tweaks "
# RPI_KERNEL_IMAGETYPE = "Image-raspberrypi4-64.bin"
BOOT_SPACE = "165536"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

