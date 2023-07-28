package nxcloud.foundation.core.data.jpa.converter.base

import jakarta.persistence.Converter
import nxcloud.foundation.core.data.jpa.converter.ConvertIfNullIntSealedEnumAttributeConverter
import nxcloud.foundation.core.lang.enumeration.YesNoStatus

@Converter(autoApply = true)
class YesNoStatusAttributeConverter : ConvertIfNullIntSealedEnumAttributeConverter<YesNoStatus>() {
    override fun convertToEntityAttributeIfNull(): YesNoStatus {
        return YesNoStatus.No
    }

    override fun convertToDatabaseColumnIfNull(): Int {
        return YesNoStatus.No.value
    }
}