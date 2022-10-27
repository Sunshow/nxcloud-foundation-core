package nxcloud.foundation.core.spring.support

import org.springframework.context.ApplicationContext

object SpringContextHelper {

    private lateinit var applicationContext: ApplicationContext

    @JvmStatic
    internal fun setApplicationContext(context: ApplicationContext) {
        applicationContext = context
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T> getBean(beanName: String): T {
        return applicationContext.getBean(beanName) as T
    }

    @JvmStatic
    fun <T> getBean(clazz: Class<T>): T {
        return applicationContext.getBean(clazz)
    }

    @JvmStatic
    fun <T> getBean(beanName: String, clazz: Class<T>): T {
        return applicationContext.getBean(beanName, clazz)
    }

}