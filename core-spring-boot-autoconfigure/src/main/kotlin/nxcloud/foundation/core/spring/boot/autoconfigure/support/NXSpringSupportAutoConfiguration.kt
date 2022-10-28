package nxcloud.foundation.core.spring.boot.autoconfigure.support

import com.github.dozermapper.core.DozerBeanMapperBuilder
import com.github.dozermapper.core.Mapper
import ma.glasnost.orika.MapperFactory
import ma.glasnost.orika.impl.DefaultMapperFactory
import nxcloud.foundation.core.bean.mapper.BeanMapperFacade
import nxcloud.foundation.core.bean.mapper.impl.dozer.DozerBeanMapperFacadeImpl
import nxcloud.foundation.core.bean.mapper.impl.orika.OrikaBeanMapperFacadeImpl
import nxcloud.foundation.core.spring.support.SpringContextHelperAware
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SpringContextHelperAware::class)
class NXSpringSupportAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SpringContextHelperAware::class)
    fun springContextHelperAware(): SpringContextHelperAware {
        return SpringContextHelperAware()
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(MapperFactory::class)
    internal class OrikaBeanMapperFacadeImplConfiguration {
        @Bean
        @ConditionalOnMissingBean(MapperFactory::class)
        fun defaultMapperFactory(): MapperFactory {
            return DefaultMapperFactory.Builder().build()
        }

        @Bean
        @ConditionalOnMissingBean(BeanMapperFacade::class)
        fun beanMapperFacade(mapperFactory: MapperFactory): BeanMapperFacade {
            return OrikaBeanMapperFacadeImpl(mapperFactory)
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Mapper::class)
    internal class DozerBeanMapperFacadeImplConfiguration {
        @Bean
        @ConditionalOnMissingBean(Mapper::class)
        fun defaultMapper(): Mapper {
            return DozerBeanMapperBuilder.buildDefault()
        }

        @Bean
        @ConditionalOnMissingBean(BeanMapperFacade::class)
        fun beanMapperFacade(mapper: Mapper): BeanMapperFacade {
            return DozerBeanMapperFacadeImpl(mapper)
        }
    }

}