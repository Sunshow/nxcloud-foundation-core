package nxcloud.foundation.core.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class LombokTest {

    @Test
    void test() {
        Assertions.assertNotNull(log);
    }

}
