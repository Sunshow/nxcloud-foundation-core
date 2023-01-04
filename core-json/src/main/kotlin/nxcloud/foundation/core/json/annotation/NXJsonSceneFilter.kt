package nxcloud.foundation.core.json.annotation

/**
 * JSON 场景过滤器：按照指定场景过滤输出的 JSON 字段
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class NXJsonSceneFilter(
    // 场景名称
    val value: Array<String> = [""],
)
