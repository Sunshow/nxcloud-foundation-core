package nxcloud.foundation.core.universal.task.registry

import nxcloud.foundation.core.universal.task.spi.UniversalTaskOperation
import nxcloud.foundation.core.universal.task.spi.impl.TestUniversalTaskIndicator
import nxcloud.foundation.core.universal.task.spi.impl.UniqueTestUniversalTaskIndicator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UniversalTaskIndicatorRegistryTest {

    @Test
    fun `should register indicator successfully`() {
        val indicator = UniqueTestUniversalTaskIndicator()

        val result = UniversalTaskIndicatorRegistry.register(indicator)

        assertTrue(result)
        val indicatorId = indicator.indicatorId()
        assertNotNull(UniversalTaskIndicatorRegistry.getIndicator(indicatorId))
        assertTrue(UniversalTaskIndicatorRegistry.getIndicatorList().contains(indicator))
    }

    @Test
    fun `should not allow duplicate registration`() {
        val indicator1 = TestUniversalTaskIndicator()
        val indicator2 = TestUniversalTaskIndicator()

        // 打印当前状态以调试
        println("测试开始前已注册的指示器：")
        UniversalTaskIndicatorRegistry.getIndicatorList().forEach {
            println("- ${it.indicatorId()} (${it.javaClass.simpleName})")
        }

        val result1 = UniversalTaskIndicatorRegistry.register(indicator1)
        val result2 = UniversalTaskIndicatorRegistry.register(indicator2)

        println("第一次注册结果: $result1")
        println("第二次注册结果: $result2")

        // 如果第一次注册失败，说明已经有同名指示器存在了，这是可以接受的
        if (!result1) {
            // 已存在同名指示器，两次注册都应该失败
            assertFalse(result1)
            assertFalse(result2)
        } else {
            // 第一次成功，第二次应该失败
            assertTrue(result1)
            assertFalse(result2)
        }

        assertEquals(1, UniversalTaskIndicatorRegistry.getIndicatorList().filter {
            it.indicatorId() == indicator1.indicatorId()
        }.size)
    }

    @Test
    fun `should unregister indicator successfully`() {
        val indicator = UniqueTestUniversalTaskIndicator()
        UniversalTaskIndicatorRegistry.register(indicator)
        val indicatorId = indicator.indicatorId()

        val unregisteredIndicator = UniversalTaskIndicatorRegistry.unregister(indicatorId)

        assertNotNull(unregisteredIndicator)
        assertEquals(indicatorId, unregisteredIndicator!!.indicatorId()) // 比较 ID 而不是对象引用
        assertNull(UniversalTaskIndicatorRegistry.getIndicator(indicatorId))
    }

    @Test
    fun `should handle operations correctly`() {
        val indicator = UniqueTestUniversalTaskIndicator()
        UniversalTaskIndicatorRegistry.register(indicator)
        val indicatorId = indicator.indicatorId()

        val registeredIndicator = UniversalTaskIndicatorRegistry.getIndicator(indicatorId)
        assertNotNull(registeredIndicator)

        val startResult = registeredIndicator!!.executeOperation(UniversalTaskOperation.START)
        val resetResult = registeredIndicator.executeOperation(UniversalTaskOperation.RESET)

        assertTrue(startResult.success)
        assertEquals("独特任务已启动", startResult.message)

        assertFalse(resetResult.success)
        assertEquals("Operation RESET is not supported", resetResult.message)
    }

    @Test
    fun `should return task list correctly`() {
        val indicator = UniqueTestUniversalTaskIndicator()
        UniversalTaskIndicatorRegistry.register(indicator)
        val indicatorId = indicator.indicatorId()

        val registeredIndicator = UniversalTaskIndicatorRegistry.getIndicator(indicatorId)
        val tasks = registeredIndicator?.getTaskList()

        assertNotNull(tasks)
        assertEquals(1, tasks?.size)
        assertEquals("unique-task-1", tasks?.get(0)?.id)
    }

    @Test
    fun `should use indicatorId from interface implementation`() {
        val indicator = TestUniversalTaskIndicator()

        assertEquals("TestUniversalTaskIndicator", indicator.indicatorId())
        val result = UniversalTaskIndicatorRegistry.register(indicator)
        assertTrue(result) // 应该能成功注册，除非已经有同名的

        assertNotNull(UniversalTaskIndicatorRegistry.getIndicator("TestUniversalTaskIndicator"))
    }
}