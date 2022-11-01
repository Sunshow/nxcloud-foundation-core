package nxcloud.foundation.core.base.deploy

data class DeployContext(
    // 机房ID
    val centerId: Int = 0,
    // 工作节点ID
    val workerId: Int = 0,
)