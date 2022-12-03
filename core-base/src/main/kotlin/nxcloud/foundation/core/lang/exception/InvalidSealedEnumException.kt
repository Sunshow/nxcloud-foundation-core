package nxcloud.foundation.core.lang.exception

open class InvalidSealedEnumException(
    message: String,
    cause: Throwable? = null
) : NXRuntimeException(message, cause)