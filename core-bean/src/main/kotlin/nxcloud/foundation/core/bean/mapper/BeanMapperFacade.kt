package nxcloud.foundation.core.bean.mapper

import nxcloud.foundation.core.lang.exception.NXRuntimeException

/**
 * 对象拷贝和映射工厂的接口
 */
interface BeanMapperFacade {

    fun <S, D> map(sourceObject: S, destinationClass: Class<D>): D

    fun <S, D> mapNullable(sourceObject: S?, destinationClass: Class<D>): D? {
        return sourceObject?.let { map(it, destinationClass) }
    }

    fun <S, D> mapList(source: Collection<S>, destinationClass: Class<D>): List<D> =
        source.map { map(it, destinationClass) }

    fun <S, D> mapList(
        source: Collection<S>,
        destinationClass: Class<D>,
        consumer: (S, D) -> Unit
    ): List<D> =
        source.map {
            val t = map(it, destinationClass)
            consumer(it, t)
            t
        }

}

/**
 * 为 kotlin 扩展便于调用的方法
 */
inline fun <S, reified D> (BeanMapperFacade).map(sourceObject: S): D {
    return map(sourceObject, D::class.java)
}

open class BeanMappingException(
    message: String,
    cause: Throwable? = null
) : NXRuntimeException(message, cause) {
    constructor(cause: Throwable) : this(cause.message ?: "", cause)

    constructor(message: String) : this(message, null)
}