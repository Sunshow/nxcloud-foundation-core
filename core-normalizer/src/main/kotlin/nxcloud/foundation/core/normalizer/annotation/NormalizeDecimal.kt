package nxcloud.foundation.core.normalizer.annotation

import nxcloud.foundation.core.normalizer.handler.NormalizeDecimalHandler

enum class DecimalNormalizeAction {
    STRIP_TRAILING_ZEROS,
    SCALE_HALF_UP,
    SCALE_HALF_DOWN,
    SCALE_FLOOR,
    SCALE_CEILING,
    ABS,
}

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@NormalizeMarker(handler = NormalizeDecimalHandler::class)
annotation class NormalizeDecimal(
    vararg val actions: DecimalNormalizeAction,
    val scale: Int = 2,
)
