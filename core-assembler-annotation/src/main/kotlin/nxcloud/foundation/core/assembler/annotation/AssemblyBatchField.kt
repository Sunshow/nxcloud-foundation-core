package nxcloud.foundation.core.assembler.annotation

/**
 * 标记可组装的包装类中的批量加载列表属性
 * 用于正向批量加载场景：从主实体的 List<ID> 字段批量加载目标实体列表
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AssemblyBatchField(
    /**
     * 来源实体中存储 ID 列表的字段名
     * 该字段需标记 @AssemblyRefSource 指向目标实体类型
     * 未指定则根据标记注解的字段按类型从来源实体类型中匹配
     */
    val entityField: String = "",
)
