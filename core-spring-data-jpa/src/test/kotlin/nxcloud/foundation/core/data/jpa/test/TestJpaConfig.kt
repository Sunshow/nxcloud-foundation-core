package nxcloud.foundation.core.data.jpa.test

import nxcloud.foundation.core.data.jpa.repository.support.AdvancedJpaRepositoryFactoryBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(
    repositoryFactoryBeanClass = AdvancedJpaRepositoryFactoryBean::class,
    basePackages = ["nxcloud.foundation.core.data.jpa.test"]
)
@EntityScan(basePackages = ["nxcloud.foundation.core.data.jpa.test"])
class TestJpaConfig {
}