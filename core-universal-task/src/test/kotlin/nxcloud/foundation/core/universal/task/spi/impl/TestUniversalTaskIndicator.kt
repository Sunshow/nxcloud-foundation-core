package nxcloud.foundation.core.universal.task.spi.impl

import nxcloud.foundation.core.universal.task.enumeration.UniversalTaskOperation
import nxcloud.foundation.core.universal.task.spi.UniversalTaskIndicator
import nxcloud.foundation.core.universal.task.spi.UniversalTaskInfo
import nxcloud.foundation.core.universal.task.spi.UniversalTaskOperationResult
import java.time.LocalDateTime

class TestUniversalTaskIndicator : UniversalTaskIndicator {

    override fun getTaskList(): List<UniversalTaskInfo> {
        return listOf(
            UniversalTaskInfo(
                id = "test-task-1",
                indicator = "TestUniversalTaskIndicator",
                name = "测试任务1",
                description = "这是一个测试任务",
                category = "test",
                current = 5,
                total = 10,
                createTime = LocalDateTime.now(),
                updateTime = LocalDateTime.now()
            ),
            UniversalTaskInfo(
                id = "test-task-2",
                indicator = "TestUniversalTaskIndicator",
                name = "测试任务2",
                description = "这是另一个测试任务",
                category = "test",
                current = 3,
                total = 8,
                createTime = LocalDateTime.now(),
                updateTime = LocalDateTime.now()
            )
        )
    }

    override fun getSupportedOperations(): Set<UniversalTaskOperation> {
        return setOf(
            UniversalTaskOperation.START,
            UniversalTaskOperation.PAUSE,
            UniversalTaskOperation.STOP
        )
    }

    override fun executeOperation(operation: UniversalTaskOperation): UniversalTaskOperationResult {
        return when (operation) {
            UniversalTaskOperation.START -> {
                UniversalTaskOperationResult.success("任务已启动")
            }

            UniversalTaskOperation.PAUSE -> {
                UniversalTaskOperationResult.success("任务已暂停")
            }

            UniversalTaskOperation.STOP -> {
                UniversalTaskOperationResult.success("任务已停止")
            }

            else -> {
                super.executeOperation(operation) // 使用接口默认实现
            }
        }
    }
}