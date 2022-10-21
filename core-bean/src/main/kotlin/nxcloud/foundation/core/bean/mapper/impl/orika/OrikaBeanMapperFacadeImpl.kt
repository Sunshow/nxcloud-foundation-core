package nxcloud.foundation.core.bean.mapper.impl.orika

import ma.glasnost.orika.MapperFactory
import nxcloud.foundation.core.bean.mapper.BeanMapperFacade

open class OrikaBeanMapperFacadeImpl(mapperFactory: MapperFactory) : BeanMapperFacade {

    private val mapperFacade = mapperFactory.mapperFacade

    override fun <S, D> map(sourceObject: S, destinationClass: Class<D>): D {
        return mapperFacade.map(sourceObject, destinationClass)
    }

}