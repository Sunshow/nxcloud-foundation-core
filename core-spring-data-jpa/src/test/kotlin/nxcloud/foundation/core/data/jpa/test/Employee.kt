package nxcloud.foundation.core.data.jpa.test

import nxcloud.foundation.core.data.jpa.entity.SoftDeleteJpaEntity
import nxcloud.foundation.core.data.support.annotation.EnableSoftDelete
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
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
    fun findByName(name: String): Employee?
}

interface EmployeeService {
    fun findByName(name: String): Employee?
}

@EnableSoftDelete
@Service
class EmployeeServiceImpl(private val employeeRepository: EmployeeRepository) : EmployeeService {

    override fun findByName(name: String): Employee? {
        return employeeRepository.findByName(name)
    }

}