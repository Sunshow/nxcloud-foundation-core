package nxcloud.foundation.core.normalizer.handler

import nxcloud.foundation.core.normalizer.annotation.FieldNormalizer
import nxcloud.foundation.core.normalizer.annotation.NormalizeString
import nxcloud.foundation.core.normalizer.annotation.StringNormalizeAction

class NormalizeStringHandler : FieldNormalizer<NormalizeString> {
    override fun normalize(annotation: NormalizeString, value: Any): Any? {
        if (value !is String) return value
        var result: String = value
        for (action in annotation.actions) {
            result = when (action) {
                StringNormalizeAction.TRIM -> result.trim()
                StringNormalizeAction.LOWERCASE -> result.lowercase()
                StringNormalizeAction.UPPERCASE -> result.uppercase()
            }
        }
        return result
    }
}
