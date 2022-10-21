package nxcloud.foundation.core.bean.mapper.impl.dozer

import com.github.dozermapper.core.Mapper
import nxcloud.foundation.core.bean.mapper.BeanMapperFacade

open class DozerBeanMapperFacadeImpl(private val dozer: Mapper) : BeanMapperFacade {

    override fun <S, D> map(sourceObject: S, destinationClass: Class<D>): D {
        return dozer.map(sourceObject, destinationClass)
    }

}