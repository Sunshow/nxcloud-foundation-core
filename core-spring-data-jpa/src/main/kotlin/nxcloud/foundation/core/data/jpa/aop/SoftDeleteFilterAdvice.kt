package nxcloud.foundation.core.data.jpa.aop

import mu.KotlinLogging
import nxcloud.foundation.core.data.jpa.constant.JpaConstants
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.hibernate.Session
import org.springframework.orm.jpa.EntityManagerFactoryUtils
import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.transaction.support.TransactionSynchronizationManager
import javax.persistence.EntityManagerFactory


open class SoftDeleteFilterAdvice(
    private val entityManagerFactory: EntityManagerFactory
) : MethodInterceptor {

    private val logger = KotlinLogging.logger {}

    override fun invoke(invocation: MethodInvocation): Any? {
        // 执行前可能当前线程还未绑定 EntityManager (例如关闭了 OpenSessionInView 情况), 所以这里需要手动确保获取EntityManager并绑定到当前线程
        val entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(
            entityManagerFactory,
            entityManagerFactory.properties
        )
            ?: run {
                logger.debug { "当前线程还未绑定 EntityManager, 自动创建并绑定" }
                val em = entityManagerFactory.createEntityManager(entityManagerFactory.properties)
                TransactionSynchronizationManager.bindResource(entityManagerFactory, EntityManagerHolder(em))
                em
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
