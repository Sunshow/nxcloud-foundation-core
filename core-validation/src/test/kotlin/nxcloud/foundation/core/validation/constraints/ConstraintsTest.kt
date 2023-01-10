package nxcloud.foundation.core.validation.constraints

import org.junit.jupiter.api.Test
import javax.validation.Validation
import javax.validation.constraints.NotBlank
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