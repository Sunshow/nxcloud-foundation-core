package springframework.child.app

import jakarta.servlet.ServletContext
import org.springframework.context.support.GenericApplicationContext
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.servlet.DispatcherServlet
import springframework.child.controller.ChildController
import springframework.child.controller.ChildControllerConfig

@RestController
class AppController(
    private val applicationContext: GenericApplicationContext,
    private val servletContext: ServletContext,
) {

    private val childControllerApplicationContext by lazy {
        val child = AnnotationConfigWebApplicationContext()
        child.register(ChildControllerConfig::class.java)
        child.parent = applicationContext

        // Register and map the REST dispatcher servlet
        val rootRestDispatcher = servletContext.addServlet("ChildDispatcher", DispatcherServlet(child))
        rootRestDispatcher.setLoadOnStartup(2)
        rootRestDispatcher.setAsyncSupported(true)

        // does not seem to be needed, leave just in case a Controller is registered in this dispatcher
        rootRestDispatcher.addMapping("/child/*")


        child
    }

    @RequestMapping("/parent")
    fun parent(): String {
        return "parent"
    }

    @RequestMapping("/loadChild")
    fun loadChild(): String {
        val childController = childControllerApplicationContext.getBean(ChildController::class.java)

        return childController.toString()
    }

}