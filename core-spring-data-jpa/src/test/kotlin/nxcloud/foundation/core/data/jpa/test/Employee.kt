package nxcloud.foundation.core.data.jpa.test

import jakarta.persistence.Entity
import jakarta.persistence.LockModeType
import jakarta.persistence.Table
import nxcloud.foundation.core.data.jpa.entity.SoftDeleteJpaEntity
import nxcloud.foundation.core.data.support.annotation.EnableSoftDelete
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.envers.Audited
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@EnableSoftDelete
@Audited
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "test_employee")
class Employee(
    var name: String,
) : SoftDeleteJpaEntity()

interface EmployeeRepository : JpaRepository<Employee, Long> {
    fun findByName(name: String): Employee?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun deleteById(id: Long)
}

interface EmployeeService {
    fun findByName(name: String): Employee?

    fun updateByName(from: String, to: String): Employee?

    fun saveByName(name: String): Employee

    fun deleteByName(name: String): Employee?

    fun deleteById(id: Long)

    fun getById(id: Long): Employee?
}

abstract class EmployeeServiceImpl(protected val employeeRepository: EmployeeRepository) : EmployeeService {

    override fun findByName(name: String): Employee? {
        return employeeRepository.findByName(name)
    }

}

// 测试注解加在子类
@Transactional(readOnly = true)
@EnableSoftDelete
@Service
class ChildEmployeeServiceImpl(employeeRepository: EmployeeRepository) :
    EmployeeServiceImpl(employeeRepository) {

    @Transactional
    override fun updateByName(from: String, to: String): Employee? {
        return employeeRepository.findByName(from)
            ?.apply {
                name = to
            }
    }

    @Transactional
    override fun saveByName(name: String): Employee {
        return employeeRepository.save(Employee(name))
    }

    @Transactional
    override fun deleteByName(name: String): Employee? {
        return employeeRepository.findByName(name)
            ?.apply {
                deleted = System.currentTimeMillis()
            }
    }

    @Transactional
    override fun deleteById(id: Long) {
        employeeRepository.deleteById(id)
    }

    override fun getById(id: Long): Employee? {
        return employeeRepository.findByIdOrNull(id)
    }

}