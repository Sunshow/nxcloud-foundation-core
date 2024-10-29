package springframework.child.controller

import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.DispatcherServlet

@ComponentScan("springframework.child.controller")
@Configuration
class ChildControllerConfig {

    @Bean
    protected fun childDispatcherServlet(
        applicationContext: ApplicationContext,
        webMvcProperties: WebMvcProperties
    ): DispatcherServlet {
        val dispatcherServlet = DispatcherServlet()
        dispatcherServlet.setDispatchOptionsRequest(webMvcProperties.isDispatchOptionsRequest)
        dispatcherServlet.setDispatchTraceRequest(webMvcProperties.isDispatchTraceRequest)
        dispatcherServlet.setPublishEvents(webMvcProperties.isPublishRequestHandledEvents)
        dispatcherServlet.isEnableLoggingRequestDetails = webMvcProperties.isLogRequestDetails
        dispatcherServlet.setApplicationContext(applicationContext)

        return dispatcherServlet
    }
}