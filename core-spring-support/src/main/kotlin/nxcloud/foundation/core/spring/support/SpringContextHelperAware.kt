package nxcloud.foundation.core.spring.support

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class SpringContextHelperAware : ApplicationContextAware {

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        SpringContextHelper.setApplicationContext(applicationContext)
    }

}