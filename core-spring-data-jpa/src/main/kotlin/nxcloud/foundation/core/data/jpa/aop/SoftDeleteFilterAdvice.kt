package nxcloud.foundation.core.data.jpa.aop

import mu.KotlinLogging
import nxcloud.foundation.core.data.jpa.constant.JpaConstants
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.hibernate.Session
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext


open class SoftDeleteFilterAdvice : MethodInterceptor {

    private val logger = KotlinLogging.logger {}

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun invoke(invocation: MethodInvocation): Any? {
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
