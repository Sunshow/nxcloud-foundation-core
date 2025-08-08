package nxcloud.foundation.core.universal.task.spi

import java.time.LocalDateTime

open class UniversalTaskInfo(
    /**
     * 任务标识, 相同 indicator 内保证唯一
     */
    val id: String,
    /**
     * 用于指示任务来源
     */
    val indicator: String,
    /**
     * 任务名称
     */
    val name: String,
    /**
     * 任务描述
     */
    val description: String? = null,
    /**
     * 任务分类, 用于分组
     */
    val category: String = "default",
    /**
     * 任务当前处理位置
     */
    val current: Int = 0,
    /**
     * 任务总共要处理的数量, 未知总数则传0
     */
    val total: Int = 0,
    /**
     * 任务创建时间
     */
    val createTime: LocalDateTime? = null,
    /**
     * 任务更新时间
     */
    val updateTime: LocalDateTime? = null,
)