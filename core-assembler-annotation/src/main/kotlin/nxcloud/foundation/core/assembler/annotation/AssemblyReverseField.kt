package nxcloud.foundation.core.assembler.annotation

import kotlin.reflect.KClass

/**
 * 标记可组装的包装类中的反向引用单个实体属性
 * 用于一对一反向关联场景：目标实体中有外键指向主实体，加载单个目标实体或提取其属性
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AssemblyReverseField(
    /**
     * 反向引用的目标实体类型（即包含 @AssemblyRefSource 指向主实体的那个类）
     */
    val target: KClass<*>,

    /**
     * 目标实体中标记了 @AssemblyRefSource 的外键字段名
     * 未指定则自动在目标实体中查找指向主实体的唯一 @AssemblyRefSource 字段
     */
    val targetField: String = "",

    /**
     * 从加载的目标实体中提取的属性名，用于填充到当前字段
     * 未指定则将整个目标实体对象填充到当前字段
     */
    val sourceProperty: String = "",
)
