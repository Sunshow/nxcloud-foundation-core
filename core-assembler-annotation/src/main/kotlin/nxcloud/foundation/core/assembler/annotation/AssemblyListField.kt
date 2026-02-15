package nxcloud.foundation.core.assembler.annotation

/**
 * 标记可组装的包装类中的反向引用列表属性
 * 用于一对多关联场景，自动从 List 泛型参数获取目标类型，
 * 并在目标类型中查找 @AssemblyRefSource 注解确定关联关系
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AssemblyListField
