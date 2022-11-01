package nxcloud.foundation.core.data.jpa.test

import nxcloud.foundation.core.data.jpa.entity.JpaEntity
import org.hibernate.annotations.GenericGenerator
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.*


@Entity
@Table(name = "test_employee")
class Employee(
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @GenericGenerator(name = "JPA", strategy = "nxcloud.foundation.core.data.jpa.id.DeployContextIdentifierGenerator")
    @GeneratedValue(generator = "JPA", strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    var name: String,
) : JpaEntity()

@Repository
interface EmployeeRepository : JpaRepository<Employee, Long> {
    fun findByName(name: String): Employee
}