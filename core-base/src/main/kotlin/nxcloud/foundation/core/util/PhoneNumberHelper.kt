package nxcloud.foundation.core.util

object PhoneNumberHelper {

    /*
     	         +---+      +------------+
             +---+ 3 +------+    0-9     +---+
             |   +---+      +------------+   |
             |                               |
             |   +---+      +------------+   |
             +---+ 4 +------+    4-9     +---+
             |   +---+      +------------+   |
             |                               |
             |   +---+      +------------+   |
             +---+ 5 +------+ 0-3 or 5-9 +---+
             |   +---+      +------------+   |
             |                               |
      +---+  |   +---+      +------------+   |  +-------+
      | 1 +------+ 6 +------+   6 or 7   +------+  0-9  |
      +---+  |   +---+      +------------+   |  +-------+
             |                               |    8位数字
             |   +---+      +------------+   |
             +---+ 7 +------+ 0,1 or 3-8 +---+
             |   +---+      +------------+   |
             |                               |
             |   +---+      +------------+   |
             +---+ 8 +------+    0-9     +---+
             |   +---+      +------------+   |
             |                               |
             |   +---+      +------------+   |
             +---+ 9 +------+    0-9     +---+
                 +---+      +------------+
     */

    /**
     * 合法手机号格式正则表达式
     */
    private val PATTERN_MOBILE_NUM = """^1(?:3\d|4[4-9]|5[0-35-9]|6[67]|7[013-8]|8\d|9\d)\d{8}$""".toRegex()

    /**
     * 手机号格式是否合法
     *
     * @param mobile 手机号
     * @return 是否合法
     */
    @JvmStatic
    fun isValidMobile(mobile: String): Boolean {
        return PATTERN_MOBILE_NUM.matches(mobile)
    }

}