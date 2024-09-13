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
)