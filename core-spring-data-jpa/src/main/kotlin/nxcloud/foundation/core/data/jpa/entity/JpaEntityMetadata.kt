package nxcloud.foundation.core.data.jpa.entity

data class JpaEntityMetadata(
    val entityType: Class<*>,
    val tableName: String,
    val enableSoftDelete: Boolean,
)
