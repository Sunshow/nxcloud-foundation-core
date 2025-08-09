package nxcloud.foundation.core.universal.task.enumeration

import nxcloud.foundation.core.lang.enumeration.IntSealedEnum

sealed class UniversalTaskOperation(
    value: Int,
    name: String,
) : IntSealedEnum(value, name) {
    data object START : UniversalTaskOperation(10, "启动")
    data object STOP : UniversalTaskOperation(20, "停止")
    data object PAUSE : UniversalTaskOperation(30, "暂停")
    data object RESUME : UniversalTaskOperation(40, "恢复")
    data object RESET : UniversalTaskOperation(50, "重置")
}