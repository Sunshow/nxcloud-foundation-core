package nxcloud.foundation.core.assembler.annotation

import kotlin.reflect.KClass

/**
 * 标记实体中特定字段来源于其他关联实体的字段
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AssemblyRefSource(
    /**
     * 来源中用于关联的字段
     */
    val sourceField: String = "",

    /**
     * 来源实体类型
     */
    val source: KClass<*>,
)