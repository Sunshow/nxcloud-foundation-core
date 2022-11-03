# nxcloud-foundation-core

## JPA

### 引入依赖

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