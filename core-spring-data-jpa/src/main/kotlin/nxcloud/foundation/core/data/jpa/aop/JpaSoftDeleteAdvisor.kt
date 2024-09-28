package nxcloud.foundation.core.data.jpa.aop

import nxcloud.foundation.core.data.support.annotation.EnableSoftDelete
import org.aopalliance.aop.Advice
import org.springframework.aop.ClassFilter
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor
import org.springframework.core.annotation.AnnotationUtils
import java.lang.reflect.Method

@Deprecated("Use nxcloud.foundation.core.data.jpa.repository.support.AdvancedJpaRepository")
open class JpaSoftDeleteAdvisor(advice: Advice) : StaticMethodMatcherPointcutAdvisor(advice) {

    override fun matches(method: Method, targetClass: Class<*>): Boolean {
        return true
    }

    override fun getClassFilter(): ClassFilter {
        return ClassFilter {
            AnnotationUtils.findAnnotation(it, EnableSoftDelete::class.java) != null
        }
    }

}