package nxcloud.foundation.core.data.jpa.aop

import nxcloud.foundation.core.data.support.annotation.EnableSoftDelete
import org.aopalliance.aop.Advice
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor
import org.springframework.core.annotation.AnnotationUtils
import java.lang.reflect.Method

open class SoftDeleteAdvisor(advice: Advice) : StaticMethodMatcherPointcutAdvisor() {

    init {
        this.advice = advice
    }

    override fun matches(method: Method, targetClass: Class<*>): Boolean {
        if (AnnotationUtils.findAnnotation(method, EnableSoftDelete::class.java) != null) {
            return true
        }
        if (AnnotationUtils.findAnnotation(targetClass, EnableSoftDelete::class.java) != null) {
            return true
        }
        return false
    }

}