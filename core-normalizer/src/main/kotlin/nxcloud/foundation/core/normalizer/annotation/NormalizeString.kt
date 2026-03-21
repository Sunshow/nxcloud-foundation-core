package nxcloud.foundation.core.normalizer.annotation

import nxcloud.foundation.core.normalizer.handler.NormalizeStringHandler

enum class StringNormalizeAction {
    TRIM,
    LOWERCASE,
    UPPERCASE,
}

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@NormalizeMarker(handler = NormalizeStringHandler::class)
annotation class NormalizeString(vararg val actions: StringNormalizeAction)
