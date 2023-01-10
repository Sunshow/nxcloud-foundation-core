package nxcloud.foundation.core.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PhoneNumberHelperTest {

    @Test
    fun testIsValid() {
        assertEquals(false, PhoneNumberHelper.isValid("12345678901"))
        assertEquals(true, PhoneNumberHelper.isValid("13800000000"))
        assertEquals(true, PhoneNumberHelper.isValid("15623671234"))
    }

}