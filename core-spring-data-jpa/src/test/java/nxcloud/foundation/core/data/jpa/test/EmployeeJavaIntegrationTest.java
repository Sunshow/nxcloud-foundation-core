package nxcloud.foundation.core.data.jpa.test;

import nxcloud.foundation.core.spring.boot.autoconfigure.support.NXSpringDataJpaAutoConfiguration;
import nxcloud.foundation.core.spring.boot.autoconfigure.support.NXSpringSupportAutoConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@ImportAutoConfiguration(classes = {AopAutoConfiguration.class, NXSpringSupportAutoConfiguration.class, NXSpringDataJpaAutoConfiguration.class})
@AutoConfigureTestDatabase
public class EmployeeJavaIntegrationTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    EmployeeService employeeService;

    @Test
    void test() {
        Assertions.assertNotNull(entityManager);
        Assertions.assertNotNull(employeeRepository);
    }

    @Test
    void testPersistent() {
        Employee employee = new Employee("John");
        entityManager.persist(employee);
        entityManager.flush();

        Employee found = employeeRepository.findByName(employee.getName());
        Assertions.assertNotNull(found);
    }
}
