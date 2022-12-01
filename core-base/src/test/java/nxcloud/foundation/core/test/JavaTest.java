package nxcloud.foundation.core.test;

import nxcloud.foundation.core.lang.enumeration.IntSealedEnum;
import nxcloud.foundation.core.lang.enumeration.YesNoStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class JavaTest {

    @Test
    void test() {
        Assertions.assertTrue(true);
    }

    @Test
    void testSealedClass() {
        List<YesNoStatus> all = IntSealedEnum.all(YesNoStatus.class);
        for (YesNoStatus yesNoStatus : all) {
            System.out.println(yesNoStatus);
        }
        Assertions.assertSame(YesNoStatus.Yes.INSTANCE, IntSealedEnum.valueOf(YesNoStatus.class, 1));
    }
}
