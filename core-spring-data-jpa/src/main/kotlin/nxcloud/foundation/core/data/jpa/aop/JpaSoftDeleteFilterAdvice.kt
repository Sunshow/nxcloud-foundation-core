package nxcloud.foundation.core.data.jpa.aop

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManagerFactory
import nxcloud.foundation.core.data.jpa.constant.JpaConstants
import nxcloud.foundation.core.data.support.context.DataQueryContextHolder
import nxcloud.foundation.core.data.support.enumeration.DataQueryMode
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
                        "未启用事务, 无法处理全局软删除逻辑"
                    }
                return invocation.proceed()
            }

        val session = entityManager.unwrap(Session::class.java)

        val context = DataQueryContextHolder.currentOrElse()

        logger.debug { "启用全局软删除处理" }

        val enabled: List<String> = when (context.queryMode) {
            DataQueryMode.NotDeleted -> {
                try {
                    session.enableFilter(JpaConstants.FILTER_NOT_DELETED)
                    listOf(JpaConstants.FILTER_NOT_DELETED)
                } catch (e: UnknownFilterException) {
                    logger.error(e) {
                        "未找到软删除过滤器: ${JpaConstants.FILTER_NOT_DELETED}"
                    }
                    emptyList()
                }
            }

            DataQueryMode.Deleted -> {
                listOfNotNull(
                    try {
                        session
                            .enableFilter(JpaConstants.FILTER_DELETED_AFTER)
                            .setParameter("deletedAfter", context.deletedAfter)
                        JpaConstants.FILTER_DELETED_AFTER
                    } catch (e: UnknownFilterException) {
                        logger.error(e) {
                            "未找到软删除过滤器: ${JpaConstants.FILTER_DELETED_AFTER}"
                        }
                        null
                    },
                    if (context.deletedBefore > 0) {
                        try {
                            session
                                .enableFilter(JpaConstants.FILTER_DELETED_BEFORE)
                                .setParameter("deletedBefore", context.deletedBefore)
                            JpaConstants.FILTER_DELETED_BEFORE
                        } catch (e: UnknownFilterException) {
                            logger.error(e) {
                                "未找到软删除过滤器: ${JpaConstants.FILTER_DELETED_BEFORE}"
                            }
                            null
                        }
                    } else {
                        null
                    }
                )
            }

            else -> {
                emptyList()
            }
        }

        val result = invocation.proceed()

        enabled
            .onEach {
                logger.debug { "关闭当前会话的软删除过滤器: $it" }
                session.disableFilter(it)
            }

        return result
    }

}
