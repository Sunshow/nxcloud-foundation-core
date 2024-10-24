package nxcloud.foundation.core.data.jpa.test

import nxcloud.foundation.core.data.support.context.DataQueryContext
import nxcloud.foundation.core.data.support.context.DataQueryContextHolder
import nxcloud.foundation.core.data.support.enumeration.DataQueryMode
import org.springframework.data.domain.Example
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController(
    private val employeeService: EmployeeService,
) {

    @RequestMapping("/save")
    fun save(): Employee {
        return employeeService.saveByName("John")
            .also {
                println("saved id: ${it.id}")
            }
    }

    @RequestMapping("/findByName")
    fun findByName(name: String): Employee? {
        val employee = employeeService.findByName(name)
        return employee
    }

    @RequestMapping("/findByNameAndAge")
    fun findByNameAndAge(name: String): List<Employee> {
        return employeeService.findByNameAndAge(name, 0)
    }

    @RequestMapping("/deleteById")
    fun deleteById(id: Long): Employee? {
        val employee = employeeService.getById(id)
        employeeService.deleteById(id)
        return employee
    }

    @RequestMapping("/findDeleted")
    fun findDeleted(id: Long): Employee? {
        DataQueryContextHolder.set(
            DataQueryContext(
                queryMode = DataQueryMode.Deleted,
                deletedAfter = 0,
                deletedBefore = 0,
            )
        )

        return employeeService.getById(id)
    }

    @RequestMapping("/deleteAllByIdInBatch")
    fun deleteAllByIdInBatch(ids: Array<Long>) {
        return employeeService.deleteAllByIdInBatch(ids.toList())
    }

    @RequestMapping("/deleteAll")
    fun deleteAll() {
        return employeeService.deleteAll()
    }

    @RequestMapping("/deleteAllInBatch")
    fun deleteAllInBatch() {
        return employeeService.deleteAllInBatch()
    }

    @RequestMapping("/exists")
    fun exists(name: String): Boolean {
        return employeeService.exists(
            Example.of(
                Employee(
                    name = name
                )
            )
        )
    }

}