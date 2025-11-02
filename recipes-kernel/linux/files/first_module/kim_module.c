#include <linux/module.h>
#include <linux/init.h>

/* Meta Information */
MODULE_LICENSE("GPL");
MODULE_AUTHOR("KIMRIHYEON");
MODULE_DESCRIPTION("A hello world ");

/**
 * @brief This function is called, when the module is loaded into the kernel
 */
static int __init my_init(void) {
	printk("Hello, KIM_RIHYEON!\n");
	return 0;
}

/**
 * @brief This function is called, when the module is removed from the kernel
 */
static void __exit my_exit(void) {
	printk("Goodbye, KIM_RIHYEON!\n");
}

module_init(my_init);
module_exit(my_exit);


