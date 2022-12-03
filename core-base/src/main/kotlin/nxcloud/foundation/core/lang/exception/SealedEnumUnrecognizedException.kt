package nxcloud.foundation.core.lang.exception

open class SealedEnumUnrecognizedException(
    message: String,
    cause: Throwable? = null
) : NXRuntimeException(message, cause)