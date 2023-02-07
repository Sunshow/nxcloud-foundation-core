package nxcloud.foundation.core.data.support.listener

/**
 * 单个 Entity 注册生命周期监听
 */
data class EntityLifecycleListenerRegistrationBean(
    val type: Class<*>,
    val listeners: List<EntityLifecycleListener>,
    // 忽略前置默认监听
    val ignorePre: Boolean = false,
    // 忽略后置默认监听
    val ignorePost: Boolean = false,
)

/**
 * 全局 Entity 注册生命周期前置监听
 */
data class DefaultPreEntityLifecycleListenerRegistrationBean(
    val listeners: List<EntityLifecycleListener>,
)

/**
 * 全局 Entity 注册生命周期后置监听
 */
data class DefaultPostEntityLifecycleListenerRegistrationBean(
    val listeners: List<EntityLifecycleListener>,
)