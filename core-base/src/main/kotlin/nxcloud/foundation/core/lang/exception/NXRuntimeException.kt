package nxcloud.foundation.core.lang.exception

open class NXRuntimeException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)