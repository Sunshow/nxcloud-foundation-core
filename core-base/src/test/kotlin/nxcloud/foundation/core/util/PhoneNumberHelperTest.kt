package nxcloud.foundation.core.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PhoneNumberHelperTest {

    @Test
    fun testIsValidMobile() {
        assertEquals(false, PhoneNumberHelper.isValidMobile("12345678901"))
        assertEquals(true, PhoneNumberHelper.isValidMobile("13800000000"))
        assertEquals(true, PhoneNumberHelper.isValidMobile("15623671234"))
    }

}