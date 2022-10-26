package nxcloud.foundation.core.idgenerator.impl.snowflake

import org.junit.jupiter.api.*
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.test.assertTrue


internal class SnowFlakeIdGeneratorTest {

    companion object {
        private val generator = SnowFlakeIdGenerator()
    }

    @Test
    fun `should print sample ids`() {
        (1..10).map { println(generator.next()) }
    }

    @Test
    fun `should extract id`() {
        val extract = generator.extract(9189721292931079)
        println(extract)
    }

    @Test
    fun `should generate unique ids`() {
        val generator = SnowFlakeIdGenerator()
        val ids = (1..1000).map { generator.next() }
        val uniqueIds = ids.toSet()
        assertTrue {
            ids.size == uniqueIds.size
        }
    }

    @RepeatedTest(1000)
    @Execution(ExecutionMode.CONCURRENT)
    fun `should concurrently generate unique ids`() {
        println(generator.next())
    }

}