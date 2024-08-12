package nxcloud.foundation.core.util

object UrlHelper {

    /**
     * 将多个字符串拼接成一个字符串, 如果有空字符串则忽略,  如果拼接后有连续的/则去掉
     */
    fun joinToUrl(vararg segments: String): String {
        val separator = "/"
        return segments
            .filter {
                it.isNotBlank()
            }
            .joinToString(separator)
            // (?<!:)：这是一个负向先行断言，表示在匹配的斜杠前面不能有冒号 :。这确保了 :// 中的斜杠不会被匹配。
            .replace(Regex("(?<!:)$separator{2,}"), separator)
    }

}