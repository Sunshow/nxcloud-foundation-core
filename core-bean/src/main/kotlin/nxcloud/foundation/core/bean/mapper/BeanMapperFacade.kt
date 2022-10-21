package nxcloud.foundation.core.bean.mapper

/**
 * 对象拷贝和映射工厂的接口
 */
interface BeanMapperFacade {

    fun <S, D> map(sourceObject: S, destinationClass: Class<D>): D

    fun <S, D> mapNullable(sourceObject: S?, destinationClass: Class<D>): D? {
        return sourceObject?.let { map(it, destinationClass) }
    }

}