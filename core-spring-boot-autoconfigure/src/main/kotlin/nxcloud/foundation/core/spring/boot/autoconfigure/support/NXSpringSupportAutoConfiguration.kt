package nxcloud.foundation.core.spring.boot.autoconfigure.support

import com.github.dozermapper.core.DozerBeanMapperBuilder
import com.github.dozermapper.core.Mapper
import ma.glasnost.orika.MapperFactory
import ma.glasnost.orika.impl.DefaultMapperFactory
import nxcloud.foundation.core.base.deploy.DeployContext
import nxcloud.foundation.core.bean.mapper.BeanMapperFacade
import nxcloud.foundation.core.bean.mapper.impl.dozer.DozerBeanMapperFacadeImpl
import nxcloud.foundation.core.bean.mapper.impl.modelmapper.ModelMapperBeanMapperFacadeImpl
import nxcloud.foundation.core.bean.mapper.impl.orika.OrikaBeanMapperFacadeImpl
import nxcloud.foundation.core.idgenerator.IdGeneratorFacade
import nxcloud.foundation.core.idgenerator.impl.snowflake.SnowFlakeIdGenerator
import nxcloud.foundation.core.idgenerator.impl.snowflake.SnowFlakeIdGeneratorFacadeImpl
import nxcloud.foundation.core.spring.boot.autoconfigure.properties.WechatProperties
import nxcloud.foundation.core.spring.support.context.SpringContextHelperAware
import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass(SpringContextHelperAware::class)
@EnableConfigurationProperties(WechatProperties::class)
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

    // 声明一个依赖 Bean，确保 SpringContextHelperAware 提前初始化
    @Bean
    fun contextDependencyEnforcer(springContextHelperAware: SpringContextHelperAware) = Unit

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
    @ConditionalOnMissingBean(BeanMapperFacade::class)
    @ConditionalOnClass(MapperFactory::class)
    internal class OrikaBeanMapperFacadeImplConfiguration {
        @Bean
        @ConditionalOnMissingBean
        fun defaultMapperFactory(): MapperFactory {
            return DefaultMapperFactory.Builder().build()
        }

        @Bean
        @ConditionalOnMissingBean
        fun beanMapperFacade(mapperFactory: MapperFactory): BeanMapperFacade {
            return OrikaBeanMapperFacadeImpl(mapperFactory)
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(BeanMapperFacade::class)
    @ConditionalOnClass(Mapper::class)
    internal class DozerBeanMapperFacadeImplConfiguration {
        @Bean
        @ConditionalOnMissingBean
        fun defaultMapper(): Mapper {
            return DozerBeanMapperBuilder.buildDefault()
        }

        @Bean
        @ConditionalOnMissingBean
        fun beanMapperFacade(mapper: Mapper): BeanMapperFacade {
            return DozerBeanMapperFacadeImpl(mapper)
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(BeanMapperFacade::class)
    @ConditionalOnClass(ModelMapper::class)
    internal class ModelMapperMapperFacadeImplConfiguration {
        @Bean
        @ConditionalOnMissingBean
        fun modelMapper(): ModelMapper {
            return ModelMapper()
        }

        @Bean
        @ConditionalOnMissingBean
        fun beanMapperFacade(modelMapper: ModelMapper): BeanMapperFacade {
            return ModelMapperBeanMapperFacadeImpl(modelMapper)
        }
    }

}