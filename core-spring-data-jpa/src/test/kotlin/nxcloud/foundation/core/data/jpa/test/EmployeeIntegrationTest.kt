package nxcloud.foundation.core.data.jpa.test

import jakarta.persistence.EntityManagerFactory
import nxcloud.foundation.core.data.jpa.interceptor.EmptyJpaSessionFactoryInterceptor
import nxcloud.foundation.core.data.support.context.DataQueryContext
import nxcloud.foundation.core.data.support.context.DataQueryContextHolder
import nxcloud.foundation.core.data.support.enumeration.DataQueryMode
import nxcloud.foundation.core.data.support.listener.DefaultPostEntityLifecycleListenerRegistrationBean
import nxcloud.foundation.core.spring.boot.autoconfigure.support.NXSpringDataJpaAutoConfiguration
import nxcloud.foundation.core.spring.boot.autoconfigure.support.NXSpringSupportAutoConfiguration
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.boot.runApplication
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Bean
import org.springframework.test.annotation.Rollback
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


@Disabled
@DataJpaTest
@EntityScan
@ImportAutoConfiguration(classes = [AopAutoConfiguration::class, NXSpringSupportAutoConfiguration::class, NXSpringDataJpaAutoConfiguration::class])
@AutoConfigureTestDatabase
class EmployeeIntegrationTest {

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Autowired
    lateinit var employeeService: EmployeeService

    @Test
    fun test() {
        assertNotNull(entityManager)
        assertNotNull(employeeRepository)
    }

    @Test
    fun testPersistent() {
        val employee = Employee(name = "John")
        entityManager.persist(employee)
        entityManager.flush()

        val found = employeeRepository.findByName(employee.name)
        assertNotNull(found)
    }

    @Test
    fun testSoftDelete() {
        val employee = employeeService.saveByName("John")

        val found = employeeService.findByName(employee.name)
        assertNotNull(found)
        assertTrue { found.deleted == 0L }

        // 软删除
        employeeService.deleteByName(employee.name)

        assertNull(employeeService.findByName(employee.name))

        // 找回已删除的
        DataQueryContextHolder.set(
            DataQueryContext(
                queryMode = DataQueryMode.Deleted,
                deletedAfter = 0,
                deletedBefore = 0,
            )
        )

        val deleted = employeeService.findByName(employee.name)

        assertNotNull(deleted)

        assertTrue {
            deleted.deleted > 0
        }

    }

    @Rollback(true)
    @Test
    fun testTransactionAndLifecycle() {
        val employee = Employee(name = "John")
        entityManager.persist(employee)
        entityManager.flush()

        assertTrue {
            employeeService.findByName(employee.name) != null
        }

        employeeService.updateByName("John", "Tom")
        assertTrue {
            employeeService.findByName("Tom") != null
        }
    }

    @Rollback(false)
    @Test
    fun testAudit() {
        val employee = Employee(name = "John")
        employeeRepository.save(employee)
        employeeRepository.flush()
    }
}


@SpringBootApplication
class TestApp {

    @Bean
    fun identifierGeneratorStrategyHibernatePropertiesCustomizer(): HibernatePropertiesCustomizer {
        return HibernatePropertiesCustomizer {
            it["hibernate.identifier_generator_strategy_provider"] =
//                "nxcloud.foundation.core.data.jpa.id.IdentityIdentifierGeneratorStrategyProvider"
                "nxcloud.foundation.core.data.jpa.id.DeployContextIdentifierGeneratorStrategyProvider"
//                "nxcloud.foundation.core.data.jpa.id.AssignedIdentifierGeneratorStrategyProvider"
        }
    }

    @Bean
    fun employeeService(employeeRepository: EmployeeRepository): EmployeeService {
        return ChildEmployeeServiceImpl(employeeRepository)
    }

    @Bean
    fun postUpdateEntityLifecycleListener(entityManagerFactory: EntityManagerFactory): PostUpdateEntityLifecycleListener {
        return PostUpdateEntityLifecycleListener(entityManagerFactory)
    }

    @Bean
    fun defaultPostEntityLifecycleListenerRegistrationBean(listener: PostUpdateEntityLifecycleListener): DefaultPostEntityLifecycleListenerRegistrationBean {
        return DefaultPostEntityLifecycleListenerRegistrationBean(
            listOf(listener)
        )
    }

    @Bean
    fun testJpaSessionFactoryInterceptor(): EmptyJpaSessionFactoryInterceptor {
        return TestJpaSessionFactoryInterceptor()
    }

}

fun main(args: Array<String>) {
    runApplication<TestApp>(*args)
}
