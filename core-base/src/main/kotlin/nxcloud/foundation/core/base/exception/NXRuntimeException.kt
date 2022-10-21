package nxcloud.foundation.core.base.exception

open class NXRuntimeException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    constructor(cause: Throwable) : this(cause.message ?: "", cause)

    constructor(message: String) : this(message, null)
}