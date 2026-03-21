package nxcloud.foundation.core.normalizer.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class NormalizeMarker(
    val handler: KClass<out FieldNormalizer<*>>,
)
