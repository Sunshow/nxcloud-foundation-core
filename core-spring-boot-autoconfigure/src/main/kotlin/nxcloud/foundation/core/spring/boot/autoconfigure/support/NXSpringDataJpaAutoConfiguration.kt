package nxcloud.foundation.core.spring.boot.autoconfigure.support

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManagerFactory
import nxcloud.foundation.core.data.jpa.context.EntityManagerInitializerHolder
import nxcloud.foundation.core.data.jpa.event.SoftDeleteEventListener
import nxcloud.foundation.core.data.jpa.interceptor.EmptyJpaSessionFactoryInterceptor
import nxcloud.foundation.core.data.jpa.repository.support.JpaEntitySupporter
import nxcloud.foundation.core.spring.support.context.SpringContextHelper
import org.hibernate.engine.spi.SessionImplementor
import org.hibernate.event.service.spi.EventListenerRegistry
import org.hibernate.event.spi.EventType
import org.hibernate.internal.SessionFactoryImpl
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.stereotype.Component


@AutoConfiguration(after = [NXSpringSupportAutoConfiguration::class])
@ConditionalOnClass(SpringContextHelper::class, SessionImplementor::class)
@EntityScan(basePackages = ["nxcloud.foundation.core.data.jpa.converter.base"])
class NXSpringDataJpaAutoConfiguration {

//    @Bean
//    @ConditionalOnMissingBean(name = ["identifierGeneratorStrategyHibernatePropertiesCustomizer"])
//    fun identifierGeneratorStrategyHibernatePropertiesCustomizer(): HibernatePropertiesCustomizer {
//        return HibernatePropertiesCustomizer {
//            it["hibernate.identifier_generator_strategy_provider"] =
//                "nxcloud.foundation.core.data.jpa.id.DeployContextIdentifierGeneratorStrategyProvider"
//        }
//    }

    @Bean
    @ConditionalOnMissingBean(name = ["advancedStatementInspectorHibernatePropertiesCustomizer"])
    fun advancedStatementInspectorHibernatePropertiesCustomizer(): HibernatePropertiesCustomizer {
        return HibernatePropertiesCustomizer {
            it["hibernate.session_factory.statement_inspector"] =
                "nxcloud.foundation.core.data.jpa.repository.jdbc.AdvancedStatementInspector"
        }
    }

    @Bean
    @ConditionalOnMissingBean
    fun jpaEntitySupporter(): JpaEntitySupporter {
        return JpaEntitySupporter()
    }

    @Bean
    @ConditionalOnMissingBean(EmptyJpaSessionFactoryInterceptor::class)
    fun emptyJpaSessionFactoryInterceptor(): EmptyJpaSessionFactoryInterceptor {
        return EmptyJpaSessionFactoryInterceptor()
    }

    @Bean
    @ConditionalOnBean(EmptyJpaSessionFactoryInterceptor::class)
    fun sessionFactoryInterceptorHibernatePropertiesCustomizer(interceptor: EmptyJpaSessionFactoryInterceptor): HibernatePropertiesCustomizer {
        return HibernatePropertiesCustomizer {
            it["hibernate.session_factory.interceptor"] = interceptor::class.java.canonicalName
        }
    }

    @Bean
    fun jpaTransactionManagerCustomizer(): TransactionManagerCustomizer<JpaTransactionManager> {
        return TransactionManagerCustomizer<JpaTransactionManager> {
            it.setEntityManagerInitializer {
                EntityManagerInitializerHolder.get()
                    .forEach { initializer ->
                        initializer(it)
                    }
            }
        }
    }

//    @Bean
//    @ConditionalOnProperty(name = ["nxcloud.jpa.soft-delete.enable"], havingValue = "true", matchIfMissing = true)
//    @ConditionalOnMissingBean(name = ["jpaSoftDeleteFilterAdvice"])
//    fun jpaSoftDeleteFilterAdvice(): JpaSoftDeleteFilterAdvice {
//        return JpaSoftDeleteFilterAdvice()
//    }
//
//    @Bean
//    @ConditionalOnProperty(name = ["nxcloud.jpa.soft-delete.enable"], havingValue = "true", matchIfMissing = true)
//    @ConditionalOnMissingBean(name = ["jpaSoftDeleteAdvisor"])
//    fun jpaSoftDeleteAdvisor(@Qualifier("jpaSoftDeleteFilterAdvice") advice: Advice): JpaSoftDeleteAdvisor {
//        return JpaSoftDeleteAdvisor(advice)
//    }

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
            sessionFactory.serviceRegistry
                .getService(
                    EventListenerRegistry::class.java
                )
                ?.apply {
                    getEventListenerGroup(EventType.PRE_DELETE)
                        .appendListener(softDeleteEventListener)
                }
        }

    }

}