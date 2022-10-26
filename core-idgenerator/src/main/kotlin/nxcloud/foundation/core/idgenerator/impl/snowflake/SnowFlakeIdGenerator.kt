package nxcloud.foundation.core.idgenerator.impl.snowflake

import java.time.Duration
import java.time.Instant

class SnowFlakeIdGenerator(
    // 选项
    val option: Option = Option(),
    // 结构
    val structure: Structure = Structure(),
    // 机房ID
    val centerId: Int = 0,
    // 工作节点ID
    val workerId: Int = 0,
) {
    init {
        val maxCenterId = calculateMask(structure.center)
        require(centerId in 0..maxCenterId) { "centerId must be between 0 (inclusive) and $maxCenterId (inclusive), but was $centerId" }

        val maxWorkerId = calculateMask(structure.worker)
        require(workerId in 0..maxWorkerId) { "workerId must be between 0 (inclusive) and $maxWorkerId (inclusive), but was $workerId" }
    }

    private val lock = Any()

    // precalculated variables for bit magic
    private val maxSequence = calculateMask(structure.sequence)
    private val maskTime = calculateMask(structure.timestamp)
    private val shiftWorker = structure.sequence
    private val shiftCenter = structure.worker + shiftWorker
    private val shiftTime = structure.center + shiftCenter

    /**
     * Tracks the last generated timestamp.
     */
    private var lastTimestamp: Long = -1

    /**
     * Sequence number, unique per timestamp.
     */
    private var sequence: Long = 0

    /**
     * Generates the next id.
     *
     * @return next id
     * @throws IllegalStateException if some invariant has been broken, e.g. the clock moved backwards or a sequence overflow occurred
     */
    operator fun next(): Long {
        val ticks = option.source.ticks
        check(ticks >= 0) { "Clock gave negative ticks" }
        val timestamp = ticks and maskTime
        synchronized(lock) {
            // Guard against non-monotonic clocks
            check(timestamp >= lastTimestamp) { "Timestamp moved backwards or wrapped around" }
            if (timestamp == lastTimestamp) {
                // Same timeslot
                if (sequence >= maxSequence) {
                    handleSequenceOverflow()
                    return next()
                }
                sequence++
            } else {
                // other timeslot, reset sequence
                sequence = 0
                lastTimestamp = timestamp
            }
            return (timestamp shl shiftTime) + (centerId shl shiftCenter) + (workerId shl shiftWorker) + sequence
        }
    }

    fun extract(id: Long): Extract {
        var leftId = id
        val timestamp = leftId shr shiftTime
        leftId -= timestamp shl shiftTime
        val centerId = leftId shr shiftCenter
        leftId -= centerId shl shiftCenter
        val workerId = leftId shr shiftWorker
        leftId -= workerId shl shiftWorker
        val sequence = leftId
        return Extract(timestamp + option.source.epoch.toEpochMilli(), centerId, workerId, sequence)
    }

    private fun handleSequenceOverflow() {
        when (option.strategy) {
            SequenceOverflowStrategy.ThrowException -> throw IllegalStateException("Sequence overflow")
            SequenceOverflowStrategy.WaitUntilNextTick -> waitForNextTick(lastTimestamp)
        }
    }

    private fun waitForNextTick(lastTimestamp: Long) {
        var timestamp: Long
        do {
            Thread.sleep(option.source.duration.toMillis())
            timestamp = option.source.ticks and maskTime
        } while (timestamp == lastTimestamp)
    }

    private fun calculateMask(bits: Int): Long {
        return (1L shl bits) - 1
    }

    data class Option(
        // 起始时间
        val source: TimeSource = MillisecondTimeSource(Instant.parse("2022-10-01T00:00:00Z")),
        // 溢出策略
        val strategy: SequenceOverflowStrategy = SequenceOverflowStrategy.WaitUntilNextTick,
    )

    data class Structure(
        // 时间戳位数
        val timestamp: Int = 41,
        // 机房位数
        val center: Int = 0,
        // 工作节点位数
        val worker: Int = 10,
        // 自增序列号位数
        val sequence: Int = 12,
    ) {
        init {
            require(timestamp >= 1) { "timestamp must no be <= 0, but was $timestamp" }
            require(!(center < 0 || center > 31)) { "center must be between 0 (inclusive) and 31 (inclusive), but was $center" }
            require(!(worker < 1 || worker > 31)) { "worker must be between 1 (inclusive) and 31 (inclusive), but was $worker" }
            require(!(sequence < 1 || sequence > 31)) { "sequence must be between 1 (inclusive) and 31 (inclusive), but was $sequence" }

            val sum = timestamp + center + worker + sequence
            require(sum == 63) { "timestamp + center + worker + sequence must be 63, but was $sum" }
        }
    }

    data class Extract(
        val timestamp: Long,
        val center: Long,
        val worker: Long,
        val sequence: Long,
    )

    enum class SequenceOverflowStrategy {
        /**
         * Throw an exception when the sequence overflows.
         */
        ThrowException,

        /**
         * Wait until the next tick and try again.
         */
        WaitUntilNextTick,
    }

    interface TimeSource {
        val ticks: Long
        val duration: Duration
        val epoch: Instant
    }

    class MillisecondTimeSource(epoch: Instant) : TimeSource {
        private val start: Long
        private val offset: Long
        override val epoch: Instant

        init {
            require(epoch.isBefore(Instant.now())) { "epoch must be before Instant.now(), but was $epoch" }

            this.epoch = epoch

            // Record creation of this time source in milliseconds
            start = Instant.now().toEpochMilli()

            // Offset elapsed time by this moment (creation time of this time source since epoch)
            offset = start - epoch.toEpochMilli()
        }

        override val ticks: Long
            get() = offset + elapsed()
        override val duration: Duration
            get() = Duration.ofMillis(1)

        private fun elapsed(): Long {
            // Calculate elapsed time since creation of this time source in milliseconds
            return Instant.now().toEpochMilli() - start
        }

        override fun toString(): String {
            return "MillisecondTimeSource{epoch=$epoch, start=$start, offset=$offset}"
        }
    }
}