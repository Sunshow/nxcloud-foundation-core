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

//    @Test
//    fun `should generate ids in order`() {
//        val generator = SnowFlakeIdGenerator()
//        val ids = (1..1000).map { generator.nextId() }
//        assertThat(ids).isSorted()
//    }
//
//    @Test
//    fun `should generate ids with the same timestamp`() {
//        val generator = SnowFlakeIdGenerator()
//        val ids = (1..1000).map { generator.nextId() }
//        val timestamps = ids.map { generator.extractTimestamp(it) }
//        assertThat(timestamps).allMatch { it == timestamps.first() }
//    }
//
//    @Test
//    fun `should generate ids with the same centerId`() {
//        val generator = SnowFlakeIdGenerator()
//        val ids = (1..1000).map { generator.nextId() }
//        val centerIds = ids.map { generator.extractCenterId(it) }
//        assertThat(centerIds).allMatch { it == centerIds.first() }
//    }
//
//    @Test
//    fun `should generate ids with the same workerId`() {
//        val generator = SnowFlakeIdGenerator()
//        val ids = (1..1000).map { generator.nextId() }
//        val workerIds = ids.map { generator.extractWorkerId(it) }
//        assertThat(workerIds).allMatch { it == workerIds.first() }
//    }
//
//    @Test
//    fun `should generate ids with the same sequence`() {
//        val generator = SnowFlakeIdGenerator()
//        val ids = (1..1000).map { generator.nextId() }
//        val sequences = ids.map { generator.extractSequence(it) }
//        assertThat(sequences).allMatch { it == sequences.first() }
//    }
//
//    @Test
//    fun `should generate ids with the same timestamp and centerId`() {
//        val generator = SnowFlakeIdGenerator()
//        val ids = (1..1000).map { generator.nextId() }
//        val timestamps = ids.map { generator.extractTimestamp(it) }
//        val centerIds = ids.map { generator.extractCenterId(it) }
//        assertThat(timestamps.zip(centerIds)).allMatch { it
}