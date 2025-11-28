package nxcloud.foundation.core.assembler.annotation

import kotlin.reflect.KClass

/**
 * 标记可组装的包装类中的来源实体属性
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AssemblyField(
    /**
     * 来源实体类型中用于获取要组装的类的关联字段
     * 未指定则根据标记注解的字段按类型从来源实体类型中匹配关
     */
    val entityField: String = "",

    /**
     * 从 entityField 加载关联对象后用于填充的目标字段
     * 未指定则将整个对象填充到标记注解的字段中
     */
    val targetField: String = "",

    /**
     * 指定自定义的填充器
     */
    val filler: KClass<AssemblableWrapperFieldFiller> = AssemblableWrapperFieldFiller::class,
)