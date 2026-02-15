package nxcloud.foundation.core.universal.task.spi.impl

import nxcloud.foundation.core.universal.task.enumeration.UniversalTaskOperation
import nxcloud.foundation.core.universal.task.spi.UniversalTaskIndicator
import nxcloud.foundation.core.universal.task.spi.UniversalTaskInfo
import nxcloud.foundation.core.universal.task.spi.UniversalTaskOperationResult
import java.time.LocalDateTime
import java.util.*

class UniqueTestUniversalTaskIndicator : UniversalTaskIndicator {

    private val uniqueId = "UniqueTestIndicator-${UUID.randomUUID()}"

    override fun indicatorName(): String = "独特测试任务指示器"

    override fun indicatorId(): String {
        return uniqueId
    }

    override fun getTaskList(): List<UniversalTaskInfo> {
        return listOf(
            UniversalTaskInfo(
                id = "unique-task-1",
                indicator = uniqueId,
                name = "独特测试任务1",
                description = "这是一个独特的测试任务",
                category = "test",
                current = 1,
                total = 5,
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
                UniversalTaskOperationResult.success("独特任务已启动")
            }

            UniversalTaskOperation.PAUSE -> {
                UniversalTaskOperationResult.success("独特任务已暂停")
            }

            UniversalTaskOperation.STOP -> {
                UniversalTaskOperationResult.success("独特任务已停止")
            }

            else -> {
                super.executeOperation(operation) // 使用接口默认实现
            }
        }
    }
}