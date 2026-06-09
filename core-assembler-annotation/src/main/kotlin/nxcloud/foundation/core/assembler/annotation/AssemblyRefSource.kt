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

    /**
     * 额外的作用域匹配字段(主实体与目标实体同名), 用于业务键非全局唯一时(如 tenantId)复合匹配
     */
    val scopeFields: Array<String> = [],
)