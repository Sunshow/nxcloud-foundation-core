package nxcloud.foundation.core.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChinaIdCardHelperTest {

    @Test
    fun testValidate() {
        assertTrue {
            ChinaIdCardHelper.isValidIdCard("11010120240801061X")
            ChinaIdCardHelper.isValidIdCard("110101202408015517")
        }
        assertFalse {
            ChinaIdCardHelper.isValidIdCard("11010120240801061X", ageMin = 10)
        }
        assertFalse {
            ChinaIdCardHelper.isValidIdCard("11010120240801062X")
        }
    }

}