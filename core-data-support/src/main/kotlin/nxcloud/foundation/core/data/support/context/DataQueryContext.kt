package nxcloud.foundation.core.data.support.context

import nxcloud.foundation.core.data.support.enumeration.DataQueryMode

open class DataQueryContext(
    /**
     * 查询模式
     */
    val queryMode: DataQueryMode = DataQueryMode.NotDeleted,
    /**
     * 查询已删除模式下的删除时间范围 (小于)
     */
    val deletedBefore: Long = 0,
    /**
     * 查询已删除模式下的删除时间范围 (大于)
     */
    val deletedAfter: Long = 0,
) {

    // 实现指定判断相同值和hashcode的方法 使 DataQueryContext 可以作为 Map 的 key
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataQueryContext) return false

        if (queryMode != other.queryMode) return false
        if (deletedBefore != other.deletedBefore) return false
        if (deletedAfter != other.deletedAfter) return false

        return true
    }

    override fun hashCode(): Int {
        var result = queryMode.hashCode()
        result = 31 * result + deletedBefore.hashCode()
        result = 31 * result + deletedAfter.hashCode()
        return result
    }

}