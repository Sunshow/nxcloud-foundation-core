package nxcloud.foundation.core.data.jpa.test

import jakarta.persistence.Entity
import jakarta.persistence.LockModeType
import jakarta.persistence.Table
import nxcloud.foundation.core.data.jpa.entity.SoftDeleteJpaEntity
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.envers.Audited
import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Audited
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "test_employee")
class Employee(
    var name: String,
    var age: Int = 0,
    var male: Boolean = true,
) : SoftDeleteJpaEntity()

interface EmployeeRepository : JpaRepository<Employee, Long> {
    fun findByName(name: String): Employee?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun deleteById(id: Long)

    fun findByNameAndAge(name: String, age: Int, page: Pageable): Page<Employee>

}

interface EmployeeService {
    fun findByName(name: String): Employee?

    fun updateByName(from: String, to: String): Employee?

    fun saveByName(name: String): Employee

    fun deleteByName(name: String): Employee?

    fun findByNameAndAge(name: String, age: Int): List<Employee>

    fun deleteById(id: Long)

    fun getById(id: Long): Employee?

    fun deleteAllByIdInBatch(ids: List<Long>)

    fun deleteAll()

    fun deleteAllInBatch()

    fun exists(example: Example<Employee>): Boolean
}

abstract class EmployeeServiceImpl(protected val employeeRepository: EmployeeRepository) : EmployeeService {

    override fun findByName(name: String): Employee? {
        return employeeRepository.findByName(name)
    }

}

// 测试注解加在子类
@Transactional(readOnly = true)
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

    @Transactional
    override fun deleteAllByIdInBatch(ids: List<Long>) {
        employeeRepository.deleteAllByIdInBatch(ids)
    }

    @Transactional
    override fun deleteAll() {
        employeeRepository.deleteAll()
    }

    @Transactional
    override fun deleteAllInBatch() {
        employeeRepository.deleteAllInBatch()
    }

    override fun exists(example: Example<Employee>): Boolean {
        return employeeRepository.exists(example)
    }

    override fun findByNameAndAge(name: String, age: Int): List<Employee> {
        return employeeRepository.findByNameAndAge(name, age, Pageable.ofSize(1)).toList()
    }
}