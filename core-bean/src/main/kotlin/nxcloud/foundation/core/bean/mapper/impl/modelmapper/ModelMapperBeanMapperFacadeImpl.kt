package nxcloud.foundation.core.bean.mapper.impl.modelmapper

import nxcloud.foundation.core.bean.mapper.BeanMapperFacade
import nxcloud.foundation.core.bean.mapper.BeanMappingException
import org.modelmapper.ModelMapper

open class ModelMapperBeanMapperFacadeImpl(private val modelMapper: ModelMapper) : BeanMapperFacade {

    override fun <S : Any, D : Any> map(sourceObject: S, destinationClass: Class<D>): D {
        try {
            return modelMapper.map(sourceObject, destinationClass)
        } catch (e: Exception) {
            throw BeanMappingException(e)
        }
    }

}