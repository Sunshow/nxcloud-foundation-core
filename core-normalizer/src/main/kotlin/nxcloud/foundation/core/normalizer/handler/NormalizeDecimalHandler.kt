package nxcloud.foundation.core.normalizer.handler

import nxcloud.foundation.core.normalizer.annotation.DecimalNormalizeAction
import nxcloud.foundation.core.normalizer.annotation.FieldNormalizer
import nxcloud.foundation.core.normalizer.annotation.NormalizeDecimal
import java.math.BigDecimal
import java.math.RoundingMode

class NormalizeDecimalHandler : FieldNormalizer<NormalizeDecimal> {
    override fun normalize(annotation: NormalizeDecimal, value: Any): Any? {
        if (value !is BigDecimal) return value
        var result: BigDecimal = value
        for (action in annotation.actions) {
            result = when (action) {
                DecimalNormalizeAction.STRIP_TRAILING_ZEROS -> result.stripTrailingZeros()
                DecimalNormalizeAction.SCALE_HALF_UP -> result.setScale(annotation.scale, RoundingMode.HALF_UP)
                DecimalNormalizeAction.SCALE_HALF_DOWN -> result.setScale(annotation.scale, RoundingMode.HALF_DOWN)
                DecimalNormalizeAction.SCALE_FLOOR -> result.setScale(annotation.scale, RoundingMode.FLOOR)
                DecimalNormalizeAction.SCALE_CEILING -> result.setScale(annotation.scale, RoundingMode.CEILING)
                DecimalNormalizeAction.ABS -> result.abs()
            }
        }
        return result
    }
}
