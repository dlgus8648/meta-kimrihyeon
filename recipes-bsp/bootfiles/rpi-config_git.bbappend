do_deploy:append() {
    printf '\n# Custom UART and Bluetooth Configuration\n' >> $CONFIG
    echo "dtoverlay=disable-bt" >> $CONFIG
    echo "dtoverlay=uart2" >> $CONFIG
    echo "enable_uart=1" >> $CONFIG
    echo "gpio=22-27=np" >> $CONFIG
    echo "enable_jtag_gpio=1" >> $CONFIG
}
