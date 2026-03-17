package com.shoplane.parceltracking.infra.outbox

import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.util.concurrent.TimeUnit

@Component
class OutboxRelay(
    private val outbox: OutboxMessageRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val clock: Clock,
) {
    @Scheduled(fixedDelayString = "\${parcel-tracking.outbox.relay-interval:PT1S}")
    @Transactional
    fun relayPendingMessages() {
        val pending = outbox.findTop100ByPublishedAtIsNullOrderByIdAsc()
        pending.forEach { message ->
            kafkaTemplate.send(toRecord(message)).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            message.publishedAt = clock.instant()
        }
        if (pending.isNotEmpty()) {
            logger.debug("Relayed {} outbox messages", pending.size)
        }
    }

    private fun toRecord(message: OutboxMessageEntity): ProducerRecord<String, String> =
        ProducerRecord<String, String>(message.topic, message.messageKey, message.payload).apply {
            headers().add(EVENT_TYPE_HEADER, message.eventType.toByteArray(StandardCharsets.UTF_8))
        }

    private companion object {
        const val EVENT_TYPE_HEADER = "event-type"
        const val SEND_TIMEOUT_SECONDS = 10L
        val logger = LoggerFactory.getLogger(OutboxRelay::class.java)
    }
}
