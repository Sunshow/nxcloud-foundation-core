package nxcloud.foundation.core.test

import nxcloud.foundation.core.lang.enumeration.IntSealedEnum
import nxcloud.foundation.core.lang.enumeration.YesNoStatus
import org.junit.jupiter.api.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class KotlinTest {

    @Test
    fun test() {
        assertTrue { true }
    }

    @Test
    fun testSealedClass() {
        val all = IntSealedEnum.all<YesNoStatus>()
        for (yesNoStatus in all) {
            println(yesNoStatus)
        }
        assertSame(YesNoStatus.Yes, IntSealedEnum.valueOf<YesNoStatus>(1))
    }

}