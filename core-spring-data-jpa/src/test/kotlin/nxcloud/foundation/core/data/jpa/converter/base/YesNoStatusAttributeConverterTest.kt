package nxcloud.foundation.core.data.jpa.converter.base

import nxcloud.foundation.core.lang.enumeration.YesNoStatus
import org.junit.jupiter.api.Test
import kotlin.test.assertSame

class YesNoStatusAttributeConverterTest {

    @Test
    fun testConvert() {
        val converter = YesNoStatusAttributeConverter()
        assertSame(YesNoStatus.Yes, converter.convertToEntityAttribute(1))
        assertSame(YesNoStatus.No, converter.convertToEntityAttribute(0))
        assertSame(YesNoStatus.No, converter.convertToEntityAttribute(10))
        assertSame(1, converter.convertToDatabaseColumn(YesNoStatus.Yes))
        assertSame(0, converter.convertToDatabaseColumn(YesNoStatus.No))
        assertSame(0, converter.convertToDatabaseColumn(null))
    }

}