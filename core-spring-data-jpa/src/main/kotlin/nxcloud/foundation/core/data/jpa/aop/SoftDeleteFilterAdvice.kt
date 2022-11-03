package nxcloud.foundation.core.data.jpa.aop

import mu.KotlinLogging
import nxcloud.foundation.core.data.jpa.constant.JpaConstants
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.hibernate.Session
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext


@Aspect
class SoftDeleteFilterAdvice {

    private val logger = KotlinLogging.logger {}

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Around("@within(nxcloud.foundation.core.data.support.annotation.EnableSoftDelete) || @annotation(nxcloud.foundation.core.data.support.annotation.EnableSoftDelete)")
    @Throws(Throwable::class)
    fun doProcess(joinPoint: ProceedingJoinPoint): Any? {
        return try {
            logger.debug { "启用当前会话的软删除过滤器: ${JpaConstants.FILTER_SOFT_DELETE}" }
            entityManager.unwrap(Session::class.java).enableFilter(JpaConstants.FILTER_SOFT_DELETE)
            joinPoint.proceed()
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
