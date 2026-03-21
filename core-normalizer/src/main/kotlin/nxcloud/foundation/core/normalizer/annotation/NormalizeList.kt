package nxcloud.foundation.core.normalizer.annotation

import nxcloud.foundation.core.normalizer.handler.NormalizeListHandler

enum class ListNormalizeAction {
    DISTINCT,
    SORT,
}

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@NormalizeMarker(handler = NormalizeListHandler::class)
annotation class NormalizeList(vararg val actions: ListNormalizeAction)
