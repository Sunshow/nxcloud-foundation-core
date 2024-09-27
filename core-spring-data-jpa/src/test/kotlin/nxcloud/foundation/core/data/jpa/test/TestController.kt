package nxcloud.foundation.core.data.jpa.test

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

}