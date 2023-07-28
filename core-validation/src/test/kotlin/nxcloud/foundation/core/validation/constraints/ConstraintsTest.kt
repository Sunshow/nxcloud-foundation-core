package nxcloud.foundation.core.validation.constraints

import jakarta.validation.Validation
import jakarta.validation.constraints.NotBlank
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ConstraintsTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun testMobileNumber() {
        assertEquals(1, validator.validate(MobileNumberModel("123")).size)
        assertEquals(0, validator.validate(MobileNumberModel("13800000000")).size)
        assertEquals(1, validator.validate(MobileNumberModel("")).size)
    }

    internal data class MobileNumberModel(
        @NotBlank
        @MobileNumber
        val mobileNumber: String,
    )
}