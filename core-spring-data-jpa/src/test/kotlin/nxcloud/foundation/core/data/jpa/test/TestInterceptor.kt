package nxcloud.foundation.core.data.jpa.test

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nxcloud.foundation.core.data.support.context.DataQueryContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

@Component
class TestInterceptor : HandlerInterceptor {

    private val logger = KotlinLogging.logger {}

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        logger.error { "TestInterceptor preHandle" }
        DataQueryContextHolder.reset()
        return super.preHandle(request, response, handler)
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        logger.error { "TestInterceptor postHandle" }
        DataQueryContextHolder.reset()
        super.postHandle(request, response, handler, modelAndView)
    }

}