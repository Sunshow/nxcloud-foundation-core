package nxcloud.foundation.core.spring.boot.autoconfigure.support

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

}