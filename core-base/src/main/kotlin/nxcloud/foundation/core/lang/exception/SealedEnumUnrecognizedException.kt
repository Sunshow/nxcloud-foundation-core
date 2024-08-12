package nxcloud.foundation.core.lang.exception

open class SealedEnumUnrecognizedException(
    message: String = "未识别的枚举值",
    cause: Throwable? = null
) : NXRuntimeException(message, cause)