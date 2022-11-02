package nxcloud.foundation.core.data.jpa.test

import nxcloud.foundation.core.data.jpa.entity.SoftDeleteJpaEntity
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.Table


@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "test_employee")
class Employee(
    var name: String,
) : SoftDeleteJpaEntity()

@Repository
interface EmployeeRepository : JpaRepository<Employee, Long> {
    fun findByName(name: String): Employee
}