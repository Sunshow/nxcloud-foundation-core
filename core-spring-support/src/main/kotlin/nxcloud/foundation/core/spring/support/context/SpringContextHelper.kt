package nxcloud.foundation.core.spring.support.context

import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
import org.springframework.core.ResolvableType

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

    @JvmStatic
    fun <T> getBeanNullable(beanName: String): T? {
        return try {
            getBean(beanName)
        } catch (e: NoSuchBeanDefinitionException) {
            null
        }
    }

    @JvmStatic
    fun <T> getBeanNullable(clazz: Class<T>): T? {
        return try {
            getBean(clazz)
        } catch (e: NoSuchBeanDefinitionException) {
            null
        }
    }

    @JvmStatic
    fun <T> getBeanNullable(beanName: String, clazz: Class<T>): T? {
        return try {
            getBean(beanName, clazz)
        } catch (e: NoSuchBeanDefinitionException) {
            null
        }
    }

    @JvmStatic
    fun <T> getBeansOfType(clazz: Class<T>): Map<String, T> {
        return applicationContext.getBeansOfType(clazz)
    }

    @JvmStatic
    fun <T> getBean(type: ResolvableType): T {
        return applicationContext.getBeanNamesForType(type)
            .first()
            .let {
                getBean(it)
            }
    }

}