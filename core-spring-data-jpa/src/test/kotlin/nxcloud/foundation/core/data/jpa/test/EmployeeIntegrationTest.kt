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
import kotlin.test.assertTrue

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

    }
}


@SpringBootApplication
class App