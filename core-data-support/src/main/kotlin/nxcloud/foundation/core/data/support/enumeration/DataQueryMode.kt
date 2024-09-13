package nxcloud.foundation.core.data.support.enumeration

import nxcloud.foundation.core.lang.enumeration.IntSealedEnum

sealed class DataQueryMode(
    value: Int,
    name: String,
) : IntSealedEnum(value, name) {
    data object None : DataQueryMode(0, "无")
    data object NotDeleted : DataQueryMode(10, "未删除")
    data object Deleted : DataQueryMode(20, "已删除")
}