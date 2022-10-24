package nxcloud.foundation.core.bean.mapper.impl.dozer

import com.github.dozermapper.core.Mapper
import nxcloud.foundation.core.bean.mapper.BeanMapperFacade
import nxcloud.foundation.core.bean.mapper.BeanMappingException

open class DozerBeanMapperFacadeImpl(private val dozer: Mapper) : BeanMapperFacade {

    override fun <S, D> map(sourceObject: S, destinationClass: Class<D>): D {
        try {
            return dozer.map(sourceObject, destinationClass)
        } catch (e: Exception) {
            throw BeanMappingException(e)
        }
    }

}