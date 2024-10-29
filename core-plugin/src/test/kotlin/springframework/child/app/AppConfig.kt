package springframework.child.app

import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.DispatcherServlet


@Configuration
@EnableConfigurationProperties(
    WebMvcProperties::class
)
class AppConfig {

//    @Bean
//    fun dispatcherServlet(applicationContext: GenericApplicationContext): DispatcherServlet {
//        val servlet = DispatcherServlet()
//
//        val rootContext = AnnotationConfigWebApplicationContext()
//        rootContext.displayName = "Self Administration Nx"
//
//        // Registers the application configuration with the root context
//        // rootContext.setConfigLocation("com.xyz.mnp.config")
//        servlet.setApplicationContext(applicationContext)
//        return servlet
//    }

    @Bean(name = [DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME])
    fun dispatcherServlet(
        applicationContext: ApplicationContext,
        webMvcProperties: WebMvcProperties,
    ): DispatcherServlet {
        val dispatcherContext = AnnotationConfigApplicationContext()
        dispatcherContext.parent = applicationContext
        dispatcherContext.refresh()

        val dispatcherServlet = DispatcherServlet()
        dispatcherServlet.setDispatchOptionsRequest(webMvcProperties.isDispatchOptionsRequest)
        dispatcherServlet.setDispatchTraceRequest(webMvcProperties.isDispatchTraceRequest)
        dispatcherServlet.setPublishEvents(webMvcProperties.isPublishRequestHandledEvents)
        dispatcherServlet.isEnableLoggingRequestDetails = webMvcProperties.isLogRequestDetails
        dispatcherServlet.setApplicationContext(dispatcherContext)

        return dispatcherServlet
    }


}