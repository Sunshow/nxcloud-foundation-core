package nxcloud.foundation.core.spring.boot.autoconfigure.support

import io.github.oshai.kotlinlogging.KotlinLogging
import nxcloud.foundation.core.universal.task.registry.UniversalTaskIndicatorRegistry
import nxcloud.foundation.core.universal.task.spi.UniversalTaskIndicator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered

@AutoConfiguration(after = [NXSpringSupportAutoConfiguration::class])
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnClass(UniversalTaskIndicatorRegistry::class)
class NXUniversalTaskAutoConfiguration(
    private val applicationContext: ApplicationContext
) {

    private val logger = KotlinLogging.logger {}

    @EventListener(ContextRefreshedEvent::class)
    fun registerSpringUniversalTaskIndicators(event: ContextRefreshedEvent) {
        logger.info { "开始自动注册 Spring Bean 形式的 UniversalTaskIndicator" }

        try {
            val springBeans = applicationContext.getBeansOfType(UniversalTaskIndicator::class.java)
            var successCount = 0
            var skipCount = 0

            for ((beanName, indicator) in springBeans) {
                try {
                    val indicatorId = indicator.indicatorId()
                    val registered = UniversalTaskIndicatorRegistry.register(indicator)

                    if (registered) {
                        logger.info { "已注册 Spring Bean UniversalTaskIndicator: $beanName ($indicatorId, ${indicator.indicatorName()})" }
                        successCount++
                    } else {
                        logger.warn { "Spring Bean UniversalTaskIndicator 已存在，跳过注册: $beanName ($indicatorId, ${indicator.indicatorName()})" }
                        skipCount++
                    }
                } catch (e: Exception) {
                    logger.error(e) { "注册 Spring Bean UniversalTaskIndicator 失败: $beanName, 错误: ${e.message}" }
                }
            }

            logger.info { "Spring Bean UniversalTaskIndicator 注册完成 - 成功: $successCount, 跳过: $skipCount, 总数: ${springBeans.size}" }
        } catch (e: Exception) {
            logger.error(e) { "自动注册 Spring Bean UniversalTaskIndicator 过程中发生异常: ${e.message}" }
        }
    }
}