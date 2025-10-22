SUMMARY = "Kernel Image"
DESCRIPTION ="asdf"
LICENSE = "MIT"

require recipes-core/images/core-image-minimal.bb

IMAGE_INSTALL += " \
    vim \
    nano \
    htop \
    make \
    gcc \
    binutils \
"

EXTRA_IMAGE_FEATURES += "tools-sdk dev-pkgs"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"