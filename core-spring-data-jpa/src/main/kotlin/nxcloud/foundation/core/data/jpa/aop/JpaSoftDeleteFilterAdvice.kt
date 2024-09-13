package nxcloud.foundation.core.data.jpa.aop

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManagerFactory
import nxcloud.foundation.core.data.jpa.constant.JpaConstants
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.hibernate.Session
import org.hibernate.UnknownFilterException
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.orm.jpa.EntityManagerFactoryUtils


open class JpaSoftDeleteFilterAdvice(
    private val entityManagerFactory: EntityManagerFactory,
    private val jpaProperties: JpaProperties,
) : MethodInterceptor {

    private val logger = KotlinLogging.logger {}

    override fun invoke(invocation: MethodInvocation): Any? {
        val entityManager = EntityManagerFactoryUtils
            .getTransactionalEntityManager(
                entityManagerFactory,
                jpaProperties.properties
            )
            ?: run {
                logger
                    .debug {
                        "未启用事务, 无法启用全局软删除过滤器: ${JpaConstants.FILTER_SOFT_DELETE}"
                    }
                return invocation.proceed()
            }

        val session = entityManager.unwrap(Session::class.java)

        logger.debug { "启用当前会话的软删除过滤器: ${JpaConstants.FILTER_SOFT_DELETE}" }
        val enabled = try {
            session.enableFilter(JpaConstants.FILTER_SOFT_DELETE)
            true
        } catch (e: UnknownFilterException) {
            logger.error(e) {
                "未找到软删除过滤器: ${JpaConstants.FILTER_SOFT_DELETE}"
            }
            false
        }

        val result = invocation.proceed()

        if (enabled) {
            logger.debug { "关闭当前会话的软删除过滤器: ${JpaConstants.FILTER_SOFT_DELETE}" }
            session.disableFilter(JpaConstants.FILTER_SOFT_DELETE)
        }

        return result
    }

}
