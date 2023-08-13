package nxcloud.foundation.core.data.jpa.test.generator

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import nxcloud.foundation.core.data.jpa.constant.JpaConstants
import nxcloud.foundation.core.data.jpa.entity.JpaEntity
import nxcloud.foundation.core.data.jpa.entity.LongIdPK
import nxcloud.foundation.core.data.jpa.id.DeployContextIdentifierGenerator
import nxcloud.foundation.core.spring.boot.autoconfigure.support.NXSpringDataJpaAutoConfiguration
import nxcloud.foundation.core.spring.boot.autoconfigure.support.NXSpringSupportAutoConfiguration
import org.hibernate.annotations.GenericGenerator
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.jpa.repository.JpaRepository

@Entity
class Person(
    @Id
    @GenericGenerator(name = JpaConstants.ID_GENERATOR_DEPLOY_CONTEXT, type = DeployContextIdentifierGenerator::class)
    @GeneratedValue(generator = JpaConstants.ID_GENERATOR_DEPLOY_CONTEXT)
    override var id: Long = 0,
    var name: String,
) : JpaEntity(), LongIdPK

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByName(name: String): Person?
}

@DataJpaTest
@EntityScan
@ImportAutoConfiguration(classes = [AopAutoConfiguration::class, NXSpringSupportAutoConfiguration::class, NXSpringDataJpaAutoConfiguration::class])
@AutoConfigureTestDatabase
class CustomIdGeneratorTest {

    @Autowired
    lateinit var dao: PersonRepository

    @Test
    fun save() {
        val person = dao.save(Person(name = "test"))
        println(person.id)
    }
}

@SpringBootApplication
class CustomIdGeneratorTestApp