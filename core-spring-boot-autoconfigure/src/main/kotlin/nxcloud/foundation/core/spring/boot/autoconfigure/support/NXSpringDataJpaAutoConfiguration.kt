package nxcloud.foundation.core.spring.boot.autoconfigure.support

import nxcloud.foundation.core.data.jpa.aop.SoftDeleteFilterAdvice
import nxcloud.foundation.core.data.jpa.event.SoftDeleteEventListener
import nxcloud.foundation.core.spring.support.SpringContextHelper
import org.hibernate.engine.spi.SessionImplementor
import org.hibernate.event.service.spi.EventListenerRegistry
import org.hibernate.event.spi.EventType
import org.hibernate.internal.SessionFactoryImpl
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.persistence.EntityManagerFactory


@AutoConfiguration(after = [NXSpringSupportAutoConfiguration::class, SpringContextHelper::class])
@ConditionalOnClass(SpringContextHelper::class, SessionImplementor::class)
@EntityScan(basePackages = ["nxcloud.foundation.core.data.jpa.converter.base"])
class NXSpringDataJpaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = ["identifierGeneratorStrategyHibernatePropertiesCustomizer"])
    fun identifierGeneratorStrategyHibernatePropertiesCustomizer(): HibernatePropertiesCustomizer {
        return HibernatePropertiesCustomizer {
            it["hibernate.identifier_generator_strategy_provider"] =
                "nxcloud.foundation.core.data.jpa.id.DeployContextIdentifierGeneratorStrategyProvider"
        }
    }

    @Bean
    @ConditionalOnMissingBean(SoftDeleteFilterAdvice::class)
    fun softDeleteFilterAdvice(): SoftDeleteFilterAdvice {
        return SoftDeleteFilterAdvice()
    }

    @Bean
    @ConditionalOnMissingBean(SoftDeleteEventListener::class)
    fun softDeleteEventListener(): SoftDeleteEventListener {
        return SoftDeleteEventListener()
    }

    @Component
    internal class HibernateEventListenerRegistry(
        private val entityManagerFactory: EntityManagerFactory,
        private val softDeleteEventListener: SoftDeleteEventListener,
    ) {

        @PostConstruct
        fun register() {
            val sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl::class.java)
            val registry = sessionFactory.serviceRegistry.getService(
                EventListenerRegistry::class.java
            )

            registry.getEventListenerGroup(EventType.PRE_DELETE).appendListener(softDeleteEventListener)
        }

    }

}