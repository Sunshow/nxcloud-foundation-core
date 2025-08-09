package nxcloud.foundation.core.universal.task.enumeration

import nxcloud.foundation.core.lang.enumeration.IntSealedEnum

sealed class UniversalTaskStatus(
    value: Int,
    name: String,
) : IntSealedEnum(value, name) {
    data object DEFAULT : UniversalTaskStatus(0, "默认")
    data object INIT : UniversalTaskStatus(10, "初始化")
    data object IN_PROGRESS : UniversalTaskStatus(20, "进行中")
    data object PAUSED : UniversalTaskStatus(30, "已暂停")

    data object COMPLETED : UniversalTaskStatus(50, "已完成")
    data object FAILED : UniversalTaskStatus(60, "已失败")
    data object CANCELLED : UniversalTaskStatus(70, "已取消")
}