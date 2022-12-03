package nxcloud.foundation.core.data.jpa.converter.base

import nxcloud.foundation.core.data.jpa.converter.ConvertIfNullIntSealedEnumAttributeConverter
import nxcloud.foundation.core.lang.enumeration.YesNoStatus
import javax.persistence.Converter

@Converter(autoApply = true)
class YesNoStatusAttributeConverter : ConvertIfNullIntSealedEnumAttributeConverter<YesNoStatus>() {
    override fun convertToEntityAttributeIfNull(): YesNoStatus {
        return YesNoStatus.No
    }

    override fun convertToDatabaseColumnIfNull(): Int {
        return YesNoStatus.No.value
    }
}