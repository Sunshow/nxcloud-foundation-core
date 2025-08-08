package nxcloud.foundation.core.universal.task.spi

interface UniversalTaskIndicator {

    fun getTaskList(): List<UniversalTaskInfo>

    fun getSupportedOperations(): Set<UniversalTaskOperation>

    fun executeOperation(operation: UniversalTaskOperation): UniversalTaskOperationResult {
        return UniversalTaskOperationResult.failure(
            "Operation $operation is not supported",
            "OPERATION_NOT_SUPPORTED"
        )
    }

    fun canExecuteOperation(operation: UniversalTaskOperation): Boolean {
        return getSupportedOperations().contains(operation)
    }
    
}