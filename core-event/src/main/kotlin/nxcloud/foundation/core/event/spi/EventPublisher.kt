package nxcloud.foundation.core.event.spi

import java.time.LocalDateTime

interface EventPublisher {

    fun publish(event: Event)

}

interface Event {
    val instant: LocalDateTime
}

abstract class AbstractEvent(
    override val instant: LocalDateTime = LocalDateTime.now(),
) : Event

class DataEvent(
    val data: Any,
) : AbstractEvent()