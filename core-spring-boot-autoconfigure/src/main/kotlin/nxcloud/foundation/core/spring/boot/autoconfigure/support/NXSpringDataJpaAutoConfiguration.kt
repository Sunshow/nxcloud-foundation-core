package nxcloud.foundation.core.spring.boot.autoconfigure.support

import nxcloud.foundation.core.data.jpa.aop.SoftDeleteFilterAdvice
import nxcloud.foundation.core.spring.support.SpringContextHelper
import org.hibernate.engine.spi.SessionImplementor
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration(after = [NXSpringSupportAutoConfiguration::class, SpringContextHelper::class])
@ConditionalOnClass(SpringContextHelper::class, SessionImplementor::class)
class NXSpringDataJpaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SoftDeleteFilterAdvice::class)
    fun softDeleteFilterAdvice(): SoftDeleteFilterAdvice {
        return SoftDeleteFilterAdvice()
    }

}