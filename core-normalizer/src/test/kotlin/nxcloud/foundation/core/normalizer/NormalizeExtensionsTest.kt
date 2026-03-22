package nxcloud.foundation.core.normalizer

import nxcloud.foundation.core.normalizer.NormalizeExtensions.normalizeField
import nxcloud.foundation.core.normalizer.NormalizeExtensions.normalizeFields
import nxcloud.foundation.core.normalizer.annotation.DecimalNormalizeAction
import nxcloud.foundation.core.normalizer.annotation.FieldNormalizer
import nxcloud.foundation.core.normalizer.annotation.ListNormalizeAction
import nxcloud.foundation.core.normalizer.annotation.NormalizeDecimal
import nxcloud.foundation.core.normalizer.annotation.NormalizeList
import nxcloud.foundation.core.normalizer.annotation.NormalizeMarker
import nxcloud.foundation.core.normalizer.annotation.NormalizeString
import nxcloud.foundation.core.normalizer.annotation.StringNormalizeAction
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class NormalizeExtensionsTest {

    // region String tests

    data class TrimDto(
        @field:NormalizeString(StringNormalizeAction.TRIM)
        var name: String? = null,
    )

    data class LowercaseDto(
        @field:NormalizeString(StringNormalizeAction.LOWERCASE)
        var code: String? = null,
    )

    data class UppercaseDto(
        @field:NormalizeString(StringNormalizeAction.UPPERCASE)
        var code: String? = null,
    )

    data class StringComboDto(
        @field:NormalizeString(StringNormalizeAction.TRIM, StringNormalizeAction.LOWERCASE)
        var code: String? = null,
    )

    @Test
    fun testTrim() {
        val dto = TrimDto(name = "  hello  ").normalizeFields()
        assertEquals("hello", dto.name)
    }

    @Test
    fun testLowercase() {
        val dto = LowercaseDto(code = "ABC").normalizeFields()
        assertEquals("abc", dto.code)
    }

    @Test
    fun testUppercase() {
        val dto = UppercaseDto(code = "abc").normalizeFields()
        assertEquals("ABC", dto.code)
    }

    @Test
    fun testStringCombo() {
        val dto = StringComboDto(code = "  ABC  ").normalizeFields()
        assertEquals("abc", dto.code)
    }

    @Test
    fun testNullStringSkipped() {
        val dto = TrimDto(name = null).normalizeFields()
        assertNull(dto.name)
    }

    // endregion

    // region List tests

    data class DistinctListDto(
        @field:NormalizeList(ListNormalizeAction.DISTINCT)
        var tags: List<String>? = null,
    )

    data class SortListDto(
        @field:NormalizeList(ListNormalizeAction.SORT)
        var tags: List<String>? = null,
    )

    data class ListComboDto(
        @field:NormalizeList(ListNormalizeAction.DISTINCT, ListNormalizeAction.SORT)
        var tags: List<String>? = null,
    )

    @Test
    fun testDistinct() {
        val dto = DistinctListDto(tags = listOf("a", "b", "a", "c")).normalizeFields()
        assertEquals(listOf("a", "b", "c"), dto.tags)
    }

    @Test
    fun testSort() {
        val dto = SortListDto(tags = listOf("c", "a", "b")).normalizeFields()
        assertEquals(listOf("a", "b", "c"), dto.tags)
    }

    @Test
    fun testListCombo() {
        val dto = ListComboDto(tags = listOf("c", "a", "b", "a")).normalizeFields()
        assertEquals(listOf("a", "b", "c"), dto.tags)
    }

    @Test
    fun testNullListSkipped() {
        val dto = DistinctListDto(tags = null).normalizeFields()
        assertNull(dto.tags)
    }

    // endregion

    // region Mixed and unannotated

    data class MixedDto(
        @field:NormalizeString(StringNormalizeAction.TRIM)
        var name: String? = null,
        var untouched: String? = null,
    )

    @Test
    fun testUnannotatedFieldUntouched() {
        val dto = MixedDto(name = "  hi  ", untouched = "  keep  ").normalizeFields()
        assertEquals("hi", dto.name)
        assertEquals("  keep  ", dto.untouched)
    }

    // endregion

    // region Custom handler extension

    class SnakeCaseHandler : FieldNormalizer<NormalizeSnakeCase> {
        override fun normalize(annotation: NormalizeSnakeCase, value: Any): Any? {
            if (value !is String) return value
            return value.replace(Regex("\\s+"), "_")
        }
    }

    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @NormalizeMarker(handler = SnakeCaseHandler::class)
    annotation class NormalizeSnakeCase

    class ReplaceHandler : FieldNormalizer<NormalizeReplace> {
        override fun normalize(annotation: NormalizeReplace, value: Any): Any? {
            if (value !is String) return value
            return value.replace(Regex(annotation.pattern), annotation.replacement)
        }
    }

    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @NormalizeMarker(handler = ReplaceHandler::class)
    annotation class NormalizeReplace(
        val pattern: String,
        val replacement: String,
    )

    data class SnakeCaseDto(
        @field:NormalizeString(StringNormalizeAction.TRIM)
        @field:NormalizeSnakeCase
        var name: String? = null,
    )

    data class CustomReplaceDto(
        @field:NormalizeReplace(pattern = "[^a-zA-Z0-9]", replacement = "")
        var code: String? = null,
    )

    @Test
    fun testCustomSnakeCaseHandler() {
        val dto = SnakeCaseDto(name = "  hello world  ").normalizeFields()
        assertEquals("hello_world", dto.name)
    }

    @Test
    fun testCustomReplaceHandler() {
        val dto = CustomReplaceDto(code = "abc-123_def").normalizeFields()
        assertEquals("abc123def", dto.code)
    }

    // endregion

    // region Decimal tests

    data class StripZerosDto(
        @field:NormalizeDecimal(DecimalNormalizeAction.STRIP_TRAILING_ZEROS)
        var amount: BigDecimal? = null,
    )

    data class ScaleHalfUpDto(
        @field:NormalizeDecimal(DecimalNormalizeAction.SCALE_HALF_UP)
        var amount: BigDecimal? = null,
    )

    data class ScaleHalfUpScale4Dto(
        @field:NormalizeDecimal(DecimalNormalizeAction.SCALE_HALF_UP, scale = 4)
        var amount: BigDecimal? = null,
    )

    data class ScaleHalfDownDto(
        @field:NormalizeDecimal(DecimalNormalizeAction.SCALE_HALF_DOWN)
        var amount: BigDecimal? = null,
    )

    data class ScaleFloorDto(
        @field:NormalizeDecimal(DecimalNormalizeAction.SCALE_FLOOR)
        var amount: BigDecimal? = null,
    )

    data class ScaleCeilingDto(
        @field:NormalizeDecimal(DecimalNormalizeAction.SCALE_CEILING)
        var amount: BigDecimal? = null,
    )

    data class AbsDto(
        @field:NormalizeDecimal(DecimalNormalizeAction.ABS)
        var amount: BigDecimal? = null,
    )

    data class DecimalComboDto(
        @field:NormalizeDecimal(DecimalNormalizeAction.SCALE_HALF_UP, DecimalNormalizeAction.STRIP_TRAILING_ZEROS)
        var amount: BigDecimal? = null,
    )

    @Test
    fun testStripTrailingZeros() {
        val dto = StripZerosDto(amount = BigDecimal("1.50000")).normalizeFields()
        assertEquals(BigDecimal("1.5"), dto.amount)
    }

    @Test
    fun testScaleHalfUpDefault() {
        val dto = ScaleHalfUpDto(amount = BigDecimal("1.235")).normalizeFields()
        assertEquals(BigDecimal("1.24"), dto.amount)
    }

    @Test
    fun testScaleHalfUpCustomScale() {
        val dto = ScaleHalfUpScale4Dto(amount = BigDecimal("1.23456789")).normalizeFields()
        assertEquals(BigDecimal("1.2346"), dto.amount)
    }

    @Test
    fun testScaleHalfDown() {
        val dto = ScaleHalfDownDto(amount = BigDecimal("1.235")).normalizeFields()
        assertEquals(BigDecimal("1.23"), dto.amount)
    }

    @Test
    fun testScaleFloor() {
        val dto = ScaleFloorDto(amount = BigDecimal("1.239")).normalizeFields()
        assertEquals(BigDecimal("1.23"), dto.amount)
    }

    @Test
    fun testScaleCeiling() {
        val dto = ScaleCeilingDto(amount = BigDecimal("1.231")).normalizeFields()
        assertEquals(BigDecimal("1.24"), dto.amount)
    }

    @Test
    fun testAbs() {
        val dto = AbsDto(amount = BigDecimal("-99.5")).normalizeFields()
        assertEquals(BigDecimal("99.5"), dto.amount)
    }

    @Test
    fun testDecimalCombo() {
        val dto = DecimalComboDto(amount = BigDecimal("1.50000")).normalizeFields()
        assertEquals(BigDecimal("1.5"), dto.amount)
    }

    @Test
    fun testNullDecimalSkipped() {
        val dto = StripZerosDto(amount = null).normalizeFields()
        assertNull(dto.amount)
    }

    // endregion

    // region Single field normalize

    data class MultiFieldDto(
        @field:NormalizeString(StringNormalizeAction.TRIM, StringNormalizeAction.LOWERCASE)
        var code: String? = null,
        @field:NormalizeString(StringNormalizeAction.TRIM)
        var name: String? = null,
    )

    @Test
    fun testNormalizeFieldByPropertyReference() {
        val dto = MultiFieldDto(code = "  ABC  ", name = "  Test  ")
        dto.normalizeField(MultiFieldDto::code)
        assertEquals("abc", dto.code)
        assertEquals("  Test  ", dto.name)
    }

    @Test
    fun testNormalizeFieldByName() {
        val dto = MultiFieldDto(code = "  ABC  ", name = "  Test  ")
        dto.normalizeField("name")
        assertEquals("  ABC  ", dto.code)
        assertEquals("Test", dto.name)
    }

    @Test
    fun testNormalizeFieldNullSkipped() {
        val dto = MultiFieldDto(code = null, name = "  Test  ")
        dto.normalizeField(MultiFieldDto::code)
        assertNull(dto.code)
        assertEquals("  Test  ", dto.name)
    }

    @Test
    fun testNormalizeFieldNonAnnotatedFieldIgnored() {
        val dto = MixedDto(name = "  hi  ", untouched = "  keep  ")
        dto.normalizeField("untouched")
        assertEquals("  hi  ", dto.name)
        assertEquals("  keep  ", dto.untouched)
    }

    // endregion
}
