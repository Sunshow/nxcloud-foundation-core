package nxcloud.foundation.core.bean.mapper.impl.orika

import ma.glasnost.orika.MapperFactory
import nxcloud.foundation.core.bean.mapper.BeanMapperFacade
import nxcloud.foundation.core.bean.mapper.BeanMappingException

open class OrikaBeanMapperFacadeImpl(mapperFactory: MapperFactory) : BeanMapperFacade {

    private val mapperFacade = mapperFactory.mapperFacade

    override fun <S, D> map(sourceObject: S, destinationClass: Class<D>): D {
        try {
            return mapperFacade.map(sourceObject, destinationClass)
        } catch (e: Exception) {
            throw BeanMappingException(e)
        }
    }

}