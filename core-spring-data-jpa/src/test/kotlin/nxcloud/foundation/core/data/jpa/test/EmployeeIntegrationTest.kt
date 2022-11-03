package nxcloud.foundation.core.data.jpa.test

import nxcloud.foundation.core.spring.boot.autoconfigure.support.NXSpringDataJpaAutoConfiguration
import nxcloud.foundation.core.spring.boot.autoconfigure.support.NXSpringSupportAutoConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Bean
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


@DataJpaTest
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
        val employee = Employee(name = "John")
        entityManager.persist(employee)
        entityManager.flush()

        val found = employeeRepository.findByName(employee.name)
        assertNotNull(found)
        assertTrue { found.deleted == 0L }

        found.deleted = System.currentTimeMillis()
        employeeRepository.save(found)
        employeeRepository.flush()

        assertTrue { found.deleted > 0 }

        assertTrue {
            employeeService.findByName(employee.name) == null
        }

    }
}


@SpringBootApplication
class App {

    @Bean
    fun identifierGeneratorStrategyHibernatePropertiesCustomizer(): HibernatePropertiesCustomizer {
        return HibernatePropertiesCustomizer {
            it["hibernate.ejb.identifier_generator_strategy_provider"] =
//                "nxcloud.foundation.core.data.jpa.id.IdentityIdentifierGeneratorStrategyProvider"
                "nxcloud.foundation.core.data.jpa.id.DeployContextIdentifierGeneratorStrategyProvider"
//                "nxcloud.foundation.core.data.jpa.id.AssignedIdentifierGeneratorStrategyProvider"
        }
    }

    @Bean
    fun employeeService(employeeRepository: EmployeeRepository): EmployeeService {
        return EmployeeServiceImpl(employeeRepository)
    }

}