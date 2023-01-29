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
        val a = AnnotationUtils.findAnnotation(targetClass, EnableSoftDelete::class.java)
        if (a != null) {
            return true
        }
        return false
    }

}