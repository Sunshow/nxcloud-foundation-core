# nxcloud-foundation-core

## JPA

### 引入依赖

### 生命周期

```kotlin
interface EntityLifecycleListener {

    fun onPrePersist(entity: Any) {

    }

    fun onPostPersist(entity: Any) {

    }

    fun onPreUpdate(entity: Any) {

    }

    fun onPostUpdate(entity: Any) {

    }

    fun onPreRemove(entity: Any) {

    }

    fun onPostRemove(entity: Any) {

    }

    fun onPostLoad(entity: Any) {

    }

}
```

#### 注册生命周期 Listener

```kotlin
/**
 * 单个 Entity 注册生命周期监听
 */
data class EntityLifecycleListenerRegistrationBean(
    val type: Class<Any>,
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
```

### 修改 `DefaultJpaEntity` 发号器

通过 Hibernate 提供的 SPI 实现

```kotlin
// 使用数据库自增
@Bean
fun identifierGeneratorStrategyHibernatePropertiesCustomizer(): HibernatePropertiesCustomizer {
    return HibernatePropertiesCustomizer {
        it["hibernate.ejb.identifier_generator_strategy_provider"] =
            "nxcloud.foundation.core.data.jpa.id.IdentityIdentifierGeneratorStrategyProvider"
    }
}

// 使用分布式部署环境发号器 (默认 SnowFlake)
@Bean
fun identifierGeneratorStrategyHibernatePropertiesCustomizer(): HibernatePropertiesCustomizer {
    return HibernatePropertiesCustomizer {
        it["hibernate.ejb.identifier_generator_strategy_provider"] =
            "nxcloud.foundation.core.data.jpa.id.DeployContextIdentifierGeneratorStrategyProvider"
    }
}

// 自主指定主键
@Bean
fun identifierGeneratorStrategyHibernatePropertiesCustomizer(): HibernatePropertiesCustomizer {
    return HibernatePropertiesCustomizer {
        it["hibernate.ejb.identifier_generator_strategy_provider"] =
            "nxcloud.foundation.core.data.jpa.id.AssignedIdentifierGeneratorStrategyProvider"
    }
}

```

可参考实现完全自定义的机制