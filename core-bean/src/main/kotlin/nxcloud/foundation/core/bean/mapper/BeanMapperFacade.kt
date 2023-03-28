package nxcloud.foundation.core.bean.mapper

import nxcloud.foundation.core.lang.exception.NXRuntimeException

/**
 * 对象拷贝和映射工厂的接口
 */
interface BeanMapperFacade {

    fun <S : Any, D : Any> map(sourceObject: S, destinationClass: Class<D>): D

    fun <S : Any, D : Any> mapNullable(sourceObject: S?, destinationClass: Class<D>): D? {
        return sourceObject?.let { map(it, destinationClass) }
    }

    fun <S : Any, D : Any> mapList(source: Collection<S>, destinationClass: Class<D>): List<D> =
        source.map { map(it, destinationClass) }

    fun <S : Any, D : Any> mapList(
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
inline fun <reified D : Any> (BeanMapperFacade).map(sourceObject: Any): D = map(sourceObject, D::class.java)

inline fun <reified D : Any> (BeanMapperFacade).mapList(source: Collection<Any>): List<D> =
    mapList(source, D::class.java)

open class BeanMappingException(
    message: String,
    cause: Throwable? = null
) : NXRuntimeException(message, cause) {
    constructor(cause: Throwable) : this(cause.message ?: "", cause)

    constructor(message: String) : this(message, null)
}