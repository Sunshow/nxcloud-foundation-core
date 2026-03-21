package nxcloud.foundation.core.normalizer.handler

import nxcloud.foundation.core.normalizer.annotation.FieldNormalizer
import nxcloud.foundation.core.normalizer.annotation.ListNormalizeAction
import nxcloud.foundation.core.normalizer.annotation.NormalizeList

class NormalizeListHandler : FieldNormalizer<NormalizeList> {
    override fun normalize(annotation: NormalizeList, value: Any): Any? {
        if (value !is List<*>) return value
        var result: List<*> = value
        for (action in annotation.actions) {
            result = when (action) {
                ListNormalizeAction.DISTINCT -> result.distinct()
                ListNormalizeAction.SORT -> result.filterNotNull().sortedWith(compareBy { it.toString() })
            }
        }
        return result
    }
}
