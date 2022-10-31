package nxcloud.foundation.core.data.jpa.test

import nxcloud.foundation.core.spring.boot.autoconfigure.support.NXSpringSupportAutoConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import kotlin.test.assertNotNull

@DataJpaTest
@ImportAutoConfiguration(classes = [NXSpringSupportAutoConfiguration::class])
@AutoConfigureTestDatabase
class EmployeeIntegrationTest {

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

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
}


@SpringBootApplication
class App