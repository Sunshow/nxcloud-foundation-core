package nxcloud.foundation.core.data.jpa.listener

import nxcloud.foundation.core.data.support.listener.DefaultPostEntityLifecycleListenerRegistrationBean
import nxcloud.foundation.core.data.support.listener.DefaultPreEntityLifecycleListenerRegistrationBean
import nxcloud.foundation.core.data.support.listener.EntityLifecycleListener
import nxcloud.foundation.core.data.support.listener.EntityLifecycleListenerRegistrationBean
import nxcloud.foundation.core.spring.support.SpringContextHelper
import javax.persistence.*

class JpaEntityLifecycleListenerAdapter {

    @PrePersist
    fun onPrePersist(entity: Any) {
        listeners(entity).forEach { it.onPrePersist(entity) }
    }

    @PostPersist
    fun onPostPersist(entity: Any) {
        listeners(entity).forEach { it.onPostPersist(entity) }
    }

    @PreUpdate
    fun onPreUpdate(entity: Any) {
        listeners(entity).forEach { it.onPreUpdate(entity) }
    }

    @PostUpdate
    fun onPostUpdate(entity: Any) {
        listeners(entity).forEach { it.onPostUpdate(entity) }
    }

    @PreRemove
    fun onPreRemove(entity: Any) {
        listeners(entity).forEach { it.onPreRemove(entity) }
    }

    @PostRemove
    fun onPostRemove(entity: Any) {
        listeners(entity).forEach { it.onPostRemove(entity) }
    }

    @PostLoad
    fun onPostLoad(entity: Any) {
        listeners(entity).forEach { it.onPostLoad(entity) }
    }

    private val defaultPreEntityLifecycleListeners: List<EntityLifecycleListener> =
        SpringContextHelper.getBeanNullable(DefaultPreEntityLifecycleListenerRegistrationBean::class.java)?.listeners
            ?: emptyList()

    private val defaultPostEntityLifecycleListeners: List<EntityLifecycleListener> =
        SpringContextHelper.getBeanNullable(DefaultPostEntityLifecycleListenerRegistrationBean::class.java)?.listeners
            ?: emptyList()

    private val entityListenerRegistrationBeanMapping: Map<Class<Any>, EntityLifecycleListenerRegistrationBean> =
        SpringContextHelper.getBeansOfType(EntityLifecycleListenerRegistrationBean::class.java)
            .values
            .associateBy {
                it.type
            }

    private fun listeners(entity: Any): List<EntityLifecycleListener> {
        val registrationBean = entityListenerRegistrationBeanMapping[entity]
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