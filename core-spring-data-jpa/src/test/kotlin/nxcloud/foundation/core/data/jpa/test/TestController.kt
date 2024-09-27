package nxcloud.foundation.core.data.jpa.test

import nxcloud.foundation.core.data.support.context.DataQueryContext
import nxcloud.foundation.core.data.support.context.DataQueryContextHolder
import nxcloud.foundation.core.data.support.enumeration.DataQueryMode
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

}