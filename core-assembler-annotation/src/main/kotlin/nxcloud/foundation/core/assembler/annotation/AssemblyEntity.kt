package nxcloud.foundation.core.assembler.annotation

/**
 * 标记可组装的包装类中要被组装的字段
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AssemblyEntity