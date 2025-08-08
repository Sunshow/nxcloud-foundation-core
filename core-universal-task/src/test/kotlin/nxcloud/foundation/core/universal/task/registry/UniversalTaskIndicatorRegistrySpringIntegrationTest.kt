package nxcloud.foundation.core.universal.task.registry

import nxcloud.foundation.core.spring.boot.autoconfigure.support.NXUniversalTaskAutoConfiguration
import nxcloud.foundation.core.universal.task.spi.impl.SpringTestUniversalTaskIndicator
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@SpringJUnitConfig(UniversalTaskIndicatorRegistrySpringIntegrationTest.TestConfiguration::class)
class UniversalTaskIndicatorRegistrySpringIntegrationTest {

    @Configuration
    @ComponentScan(basePackageClasses = [SpringTestUniversalTaskIndicator::class])
    @Import(NXUniversalTaskAutoConfiguration::class)
    class TestConfiguration

    @Test
    fun `should automatically register Spring Bean UniversalTaskIndicators`() {
        // 打印所有注册的指示器
        val allIndicators = UniversalTaskIndicatorRegistry.getIndicatorList()
        println("所有注册的指示器：")
        allIndicators.forEach { indicator ->
            println("- ${indicator.indicatorId()} (${indicator.javaClass.simpleName})")
        }
        
        // 验证 Spring Bean 已自动注册到 Registry
        val indicator = UniversalTaskIndicatorRegistry.getIndicator("springTestUniversalTaskIndicator")
        
        assertNotNull(indicator, "Spring Bean UniversalTaskIndicator should be automatically registered")
        assertTrue(indicator is SpringTestUniversalTaskIndicator, "Should be instance of SpringTestUniversalTaskIndicator")
        
        // 验证任务列表
        val tasks = indicator.getTaskList()
        assertTrue(tasks.isNotEmpty(), "Should have tasks")
        assertTrue(tasks.any { it.id == "spring-task-1" }, "Should contain spring-task-1")
        
        // 验证支持的操作
        val supportedOps = indicator.getSupportedOperations()
        assertTrue(supportedOps.isNotEmpty(), "Should have supported operations")
    }

    @Test
    fun `should have both SPI and Spring Bean indicators registered`() {
        val allIndicators = UniversalTaskIndicatorRegistry.getIndicatorList()
        
        // 应该包含 Spring Bean 指示器
        val springBeanIndicator = allIndicators.find { it is SpringTestUniversalTaskIndicator }
        assertNotNull(springBeanIndicator, "Should contain Spring Bean indicator")
        
        println("注册的指示器数量: ${allIndicators.size}")
        allIndicators.forEach { indicator ->
            println("指示器: ${indicator.indicatorId()} - ${indicator.javaClass.simpleName}")
        }
    }
}