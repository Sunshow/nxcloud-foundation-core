package nxcloud.foundation.core.data.jpa.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.*
import nxcloud.foundation.core.data.support.listener.DefaultPostEntityLifecycleListenerRegistrationBean
import nxcloud.foundation.core.data.support.listener.DefaultPreEntityLifecycleListenerRegistrationBean
import nxcloud.foundation.core.data.support.listener.EntityLifecycleListener
import nxcloud.foundation.core.data.support.listener.EntityLifecycleListenerRegistrationBean
import nxcloud.foundation.core.spring.support.SpringContextHelper

class JpaEntityLifecycleListenerAdapter {

    private val logger = KotlinLogging.logger {}

    @PrePersist
    fun onPrePersist(entity: Any) {
        logger.debug { "onPrePersist: $entity" }
        listeners(entity).forEach { it.onPrePersist(entity) }
    }

    @PostPersist
    fun onPostPersist(entity: Any) {
        logger.debug { "onPostPersist: $entity" }
        listeners(entity).forEach { it.onPostPersist(entity) }
    }

    @PreUpdate
    fun onPreUpdate(entity: Any) {
        logger.debug { "onPreUpdate: $entity" }
        listeners(entity).forEach { it.onPreUpdate(entity) }
    }

    @PostUpdate
    fun onPostUpdate(entity: Any) {
        logger.debug { "onPostUpdate: $entity" }
        listeners(entity).forEach { it.onPostUpdate(entity) }
    }

    @PreRemove
    fun onPreRemove(entity: Any) {
        logger.debug { "onPreRemove: $entity" }
        listeners(entity).forEach { it.onPreRemove(entity) }
    }

    @PostRemove
    fun onPostRemove(entity: Any) {
        logger.debug { "onPostRemove: $entity" }
        listeners(entity).forEach { it.onPostRemove(entity) }
    }

    @PostLoad
    fun onPostLoad(entity: Any) {
        logger.debug { "onPostLoad: $entity" }
        listeners(entity).forEach { it.onPostLoad(entity) }
    }

    private val defaultPreEntityLifecycleListeners: List<EntityLifecycleListener> by lazy {
        SpringContextHelper.getBeanNullable(DefaultPreEntityLifecycleListenerRegistrationBean::class.java)?.listeners
            ?: emptyList()
    }


    private val defaultPostEntityLifecycleListeners: List<EntityLifecycleListener> by lazy {
        SpringContextHelper.getBeanNullable(DefaultPostEntityLifecycleListenerRegistrationBean::class.java)?.listeners
            ?: emptyList()
    }

    private val entityListenerRegistrationBeanMapping: Map<Class<*>, EntityLifecycleListenerRegistrationBean> by lazy {
        SpringContextHelper.getBeansOfType(EntityLifecycleListenerRegistrationBean::class.java)
            .values
            .associateBy {
                it.type
            }
    }

    private fun listeners(entity: Any): List<EntityLifecycleListener> {
        val registrationBean = entityListenerRegistrationBeanMapping[entity::class.java]
            ?: return defaultPreEntityLifecycleListeners + defaultPostEntityLifecycleListeners

        return if (registrationBean.ignorePre && registrationBean.ignorePost) {
            registrationBean.listeners
        } else if (registrationBean.ignorePre) {
            registrationBean.listeners + defaultPostEntityLifecycleListeners
        } else if (registrationBean.ignorePost) {
            defaultPreEntityLifecycleListeners + registrationBean.listeners
        } else {
            defaultPreEntityLifecycleListeners + registrationBean.listeners + defaultPostEntityLifecycleListeners
        }
    }
}