package nxcloud.foundation.core.spring.boot.autoconfigure.support

import nxcloud.foundation.core.data.jpa.context.EntityManagerInitializerHolder
import nxcloud.foundation.core.data.jpa.softdelete.HibernateSoftDeleteEntityManagerInitializer
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean

class HibernateSoftDeleteEntityManagerFactoryBeanPostProcessor(
    private val softDeleteInitializer: HibernateSoftDeleteEntityManagerInitializer,
) : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        if (bean is AbstractEntityManagerFactoryBean) {
            bean.setEntityManagerInitializer { entityManager ->
                softDeleteInitializer.initialize(entityManager)
                EntityManagerInitializerHolder.get()
                    .forEach { initializer ->
                        initializer(entityManager)
                    }
            }
        }
        return bean
    }
}
