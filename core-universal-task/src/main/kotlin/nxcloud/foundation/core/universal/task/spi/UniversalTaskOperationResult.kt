package nxcloud.foundation.core.universal.task.spi

data class UniversalTaskOperationResult(
    val success: Boolean,
    val message: String? = null,
    val errorCode: String? = null,
    val details: Map<String, Any> = emptyMap(),
    val exception: Throwable? = null
) {
    companion object {
        fun success(message: String? = null): UniversalTaskOperationResult {
            return UniversalTaskOperationResult(
                success = true,
                message = message
            )
        }

        fun failure(message: String, errorCode: String? = null): UniversalTaskOperationResult {
            return UniversalTaskOperationResult(
                success = false,
                message = message,
                errorCode = errorCode
            )
        }

        fun failure(exception: Throwable, errorCode: String? = null): UniversalTaskOperationResult {
            return UniversalTaskOperationResult(
                success = false,
                message = exception.message,
                errorCode = errorCode,
                exception = exception
            )
        }

        fun withDetails(success: Boolean, message: String?, details: Map<String, Any>): UniversalTaskOperationResult {
            return UniversalTaskOperationResult(
                success = success,
                message = message,
                details = details
            )
        }
    }
}