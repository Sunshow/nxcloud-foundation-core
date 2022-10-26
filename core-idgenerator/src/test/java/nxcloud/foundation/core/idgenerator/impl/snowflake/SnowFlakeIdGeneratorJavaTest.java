package nxcloud.foundation.core.idgenerator.impl.snowflake;

import org.junit.jupiter.api.Test;

class SnowFlakeIdGeneratorJavaTest {

    @Test
    void test() {
        SnowFlakeIdGenerator generator = new SnowFlakeIdGenerator();
        for (int i = 0; i < 10; i++) {
            System.out.println(generator.next());
        }
    }
}
