package nxcloud.foundation.core.data.jpa.test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class TestWebConfig : WebMvcConfigurer {

    @Autowired
    private lateinit var testInterceptor: TestInterceptor

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(testInterceptor)
            .addPathPatterns("/**")
    }

}