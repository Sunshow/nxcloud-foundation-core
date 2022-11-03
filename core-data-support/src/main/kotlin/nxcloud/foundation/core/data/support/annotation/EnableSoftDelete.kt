package nxcloud.foundation.core.data.support.annotation

/**
 * 启用软删除
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class EnableSoftDelete
