package nxcloud.foundation.core.data.jpa.test

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(
    repositoryFactoryBeanClass = TestRepositoryFactoryBean::class,
    basePackages = ["nxcloud.foundation.core.data.jpa.test"]
)
@EntityScan(basePackages = ["nxcloud.foundation.core.data.jpa.test"])
class TestJpaConfig {
}