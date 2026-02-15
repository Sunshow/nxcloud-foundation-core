package nxcloud.foundation.core.assembler.component

import kotlin.reflect.KClass

/**
 * 引用源字段加载器接口
 * 用于声明某个 POJO 类型支持按指定字段加载
 *
 * @param T POJO 类型
 */
interface RefSourceFieldLoader<T : Any> {

    fun getSourceType(): KClass<T>

    fun getSourceFieldName(): String

    fun load(fieldValue: Any): T?

    fun loadBatch(fieldValues: Set<Any>): Map<Any, T>
}
