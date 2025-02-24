package nxcloud.foundation.core.spring.support.context

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class SpringContextHelperAware : ApplicationContextAware {

    private val logger = KotlinLogging.logger {}

    init {
        logger.info {
            "SpringContextHelperAware instance created!"
        }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        logger.info {
            "SpringContextHelperAware setApplicationContext() called!"
        }
        SpringContextHelper.setApplicationContext(applicationContext)
    }

}