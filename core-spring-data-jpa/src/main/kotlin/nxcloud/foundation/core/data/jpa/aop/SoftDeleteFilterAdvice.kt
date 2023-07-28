package nxcloud.foundation.core.data.jpa.aop

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManagerFactory
import nxcloud.foundation.core.data.jpa.constant.JpaConstants
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.hibernate.Session
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.orm.jpa.EntityManagerFactoryUtils
import org.springframework.orm.jpa.ExtendedEntityManagerCreator


open class SoftDeleteFilterAdvice(
    private val entityManagerFactory: EntityManagerFactory,
    private val jpaProperties: JpaProperties,
) : MethodInterceptor {

    private val logger = KotlinLogging.logger {}

    override fun invoke(invocation: MethodInvocation): Any? {
        // 执行前可能当前线程还未绑定 EntityManager (例如关闭了 OpenSessionInView 情况), 所以这里需要手动确保获取EntityManager并绑定到当前线程
        val entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(
            entityManagerFactory,
            jpaProperties.properties
        )
            ?: run {
                logger.debug { "当前线程还未绑定 EntityManager, 自动创建并绑定" }
                ExtendedEntityManagerCreator.createContainerManagedEntityManager(
                    entityManagerFactory,
                    jpaProperties.properties
                )
            }
        return try {
            logger.debug { "启用当前会话的软删除过滤器: ${JpaConstants.FILTER_SOFT_DELETE}" }
            entityManager.unwrap(Session::class.java).enableFilter(JpaConstants.FILTER_SOFT_DELETE)
            invocation.proceed()
        } catch (e: Throwable) {
            logger.error(e) {
                "启用当前会话的软删除过滤器出错"
            }
            throw e
        } finally {
            entityManager.unwrap(Session::class.java).disableFilter(JpaConstants.FILTER_SOFT_DELETE)
            logger.debug { "关闭当前会话的软删除过滤器: ${JpaConstants.FILTER_SOFT_DELETE}" }
        }
    }

}
