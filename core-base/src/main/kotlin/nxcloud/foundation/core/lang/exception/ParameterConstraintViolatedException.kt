package nxcloud.foundation.core.lang.exception

class ParameterConstraintViolatedException(
    message: String = "参数违反约束",
    cause: Throwable? = null,
    val constraints: List<ParameterConstraintViolation> = listOf()
) : NXRuntimeException(message, cause)

data class ParameterConstraintViolation(val field: String, val message: String)