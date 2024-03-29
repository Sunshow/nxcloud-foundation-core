package nxcloud.foundation.core.data.jpa.test

import io.github.oshai.kotlinlogging.KotlinLogging
import nxcloud.foundation.core.data.jpa.interceptor.EmptyJpaSessionFactoryInterceptor
import org.hibernate.Transaction

class TestJpaSessionFactoryInterceptor : EmptyJpaSessionFactoryInterceptor() {

    private val logger = KotlinLogging.logger {}

    override fun afterTransactionCompletion(tx: Transaction) {
        logger.info { "afterTransactionCompletion" }
    }
}