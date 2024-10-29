package springframework.child.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ChildController {

    @RequestMapping("/child")
    fun child(): String {
        return "child"
    }

}