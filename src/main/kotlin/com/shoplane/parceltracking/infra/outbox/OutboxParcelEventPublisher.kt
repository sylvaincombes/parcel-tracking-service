package com.shoplane.parceltracking.infra.outbox

import com.shoplane.parceltracking.domain.event.ParcelDomainEvent
import com.shoplane.parceltracking.domain.event.ParcelEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import java.time.Clock

@Component
class OutboxParcelEventPublisher(
    private val outbox: OutboxMessageRepository,
    private val jsonMapper: JsonMapper,
    private val topics: OutboxTopics,
    private val clock: Clock,
) : ParcelEventPublisher {
    @Transactional(propagation = Propagation.MANDATORY)
    override fun publish(event: ParcelDomainEvent) {
        outbox.save(
            OutboxMessageEntity(
                topic = topics.parcelEvents,
                messageKey = event.parcelId.toString(),
                eventType = ParcelEventPayloads.eventTypeOf(event),
                payload = jsonMapper.writeValueAsString(ParcelEventPayloads.payloadOf(event)),
                createdAt = clock.instant(),
            ),
        )
    }
}
