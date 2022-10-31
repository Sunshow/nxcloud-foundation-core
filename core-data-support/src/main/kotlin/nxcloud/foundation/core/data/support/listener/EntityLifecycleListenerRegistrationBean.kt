package nxcloud.foundation.core.data.support.listener

data class EntityLifecycleListenerRegistrationBean(
    val type: Class<Any>,
    val listeners: List<EntityLifecycleListener>,
    val ignorePre: Boolean = false,
    val ignorePost: Boolean = false,
)

data class DefaultPreEntityLifecycleListenerRegistrationBean(
    val listeners: List<EntityLifecycleListener>,
)

data class DefaultPostEntityLifecycleListenerRegistrationBean(
    val listeners: List<EntityLifecycleListener>,
)