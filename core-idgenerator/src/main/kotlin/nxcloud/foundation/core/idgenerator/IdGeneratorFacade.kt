package nxcloud.foundation.core.idgenerator

import nxcloud.foundation.core.base.exception.NXRuntimeException
import java.io.Serializable

/**
 * ID 生成器
 */
interface IdGeneratorFacade<ID : Serializable> {

    fun nextId(): ID

}

open class IdGenerateException(
    message: String,
    cause: Throwable? = null
) : NXRuntimeException(message, cause) {
    constructor(cause: Throwable) : this(cause.message ?: "", cause)

    constructor(message: String) : this(message, null)
}