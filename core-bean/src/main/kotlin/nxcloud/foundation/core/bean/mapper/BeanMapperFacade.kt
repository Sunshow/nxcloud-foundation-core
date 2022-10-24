package nxcloud.foundation.core.bean.mapper

import nxcloud.foundation.core.base.exception.NXRuntimeException

/**
 * 对象拷贝和映射工厂的接口
 */
interface BeanMapperFacade {

    fun <S, D> map(sourceObject: S, destinationClass: Class<D>): D

    fun <S, D> mapNullable(sourceObject: S?, destinationClass: Class<D>): D? {
        return sourceObject?.let { map(it, destinationClass) }
    }

}

open class BeanMappingException(
    message: String,
    cause: Throwable? = null
) : NXRuntimeException(message, cause) {
    constructor(cause: Throwable) : this(cause.message ?: "", cause)

    constructor(message: String) : this(message, null)
}