package nxcloud.foundation.core.universal.task.spi.impl

import nxcloud.foundation.core.universal.task.spi.UniversalTaskIndicator
import nxcloud.foundation.core.universal.task.spi.UniversalTaskInfo
import nxcloud.foundation.core.universal.task.spi.UniversalTaskOperation
import nxcloud.foundation.core.universal.task.spi.UniversalTaskOperationResult
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component("springTestUniversalTaskIndicator")
class SpringTestUniversalTaskIndicator : UniversalTaskIndicator {

    override fun indicatorId(): String {
        return "springTestUniversalTaskIndicator"
    }

    override fun getTaskList(): List<UniversalTaskInfo> {
        return listOf(
            UniversalTaskInfo(
                id = "spring-task-1",
                indicator = "SpringTestUniversalTaskIndicator",
                name = "Spring测试任务1",
                description = "这是一个Spring管理的测试任务",
                category = "spring-test",
                current = 7,
                total = 15,
                createTime = LocalDateTime.now(),
                updateTime = LocalDateTime.now()
            )
        )
    }

    override fun getSupportedOperations(): Set<UniversalTaskOperation> {
        return setOf(
            UniversalTaskOperation.START,
            UniversalTaskOperation.PAUSE,
            UniversalTaskOperation.STOP,
            UniversalTaskOperation.RESTART
        )
    }

    override fun executeOperation(operation: UniversalTaskOperation): UniversalTaskOperationResult {
        return when (operation) {
            UniversalTaskOperation.START -> {
                UniversalTaskOperationResult.success("Spring任务已启动")
            }
            UniversalTaskOperation.PAUSE -> {
                UniversalTaskOperationResult.success("Spring任务已暂停")
            }
            UniversalTaskOperation.STOP -> {
                UniversalTaskOperationResult.success("Spring任务已停止")
            }
            UniversalTaskOperation.RESTART -> {
                UniversalTaskOperationResult.success("Spring任务已重启")
            }
            else -> {
                UniversalTaskOperationResult.failure(
                    "操作 $operation 不被支持",
                    "OPERATION_NOT_SUPPORTED"
                )
            }
        }
    }
}