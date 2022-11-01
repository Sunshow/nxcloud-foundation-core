package nxcloud.foundation.core.spring.boot.autoconfigure.support

import com.github.dozermapper.core.DozerBeanMapperBuilder
import com.github.dozermapper.core.Mapper
import ma.glasnost.orika.MapperFactory
import ma.glasnost.orika.impl.DefaultMapperFactory
import nxcloud.foundation.core.base.deploy.DeployContext
import nxcloud.foundation.core.bean.mapper.BeanMapperFacade
import nxcloud.foundation.core.bean.mapper.impl.dozer.DozerBeanMapperFacadeImpl
import nxcloud.foundation.core.bean.mapper.impl.orika.OrikaBeanMapperFacadeImpl
import nxcloud.foundation.core.idgenerator.IdGeneratorFacade
import nxcloud.foundation.core.idgenerator.impl.snowflake.SnowFlakeIdGenerator
import nxcloud.foundation.core.idgenerator.impl.snowflake.SnowFlakeIdGeneratorFacadeImpl
import nxcloud.foundation.core.spring.support.SpringContextHelperAware
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SpringContextHelperAware::class)
class NXSpringSupportAutoConfiguration {

    @Value("\${nxcloud.deploy.context.center-id:0}")
    var centerId: Int = 0

    @Value("\${nxcloud.deploy.context.worker-id:0}")
    var workerId: Int = 0

    @Bean
    @ConditionalOnMissingBean(SpringContextHelperAware::class)
    fun springContextHelperAware(): SpringContextHelperAware {
        return SpringContextHelperAware()
    }

    @Bean
    @ConditionalOnMissingBean(DeployContext::class)
    fun deployContext(): DeployContext {
        return DeployContext(centerId, workerId)
    }

    @Bean
    @ConditionalOnMissingBean(IdGeneratorFacade::class)
    fun idGeneratorFacade(deployContext: DeployContext): IdGeneratorFacade<Long> {
        return SnowFlakeIdGeneratorFacadeImpl(
            generator = SnowFlakeIdGenerator(
                centerId = deployContext.centerId,
                workerId = deployContext.workerId,
            )
        )
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