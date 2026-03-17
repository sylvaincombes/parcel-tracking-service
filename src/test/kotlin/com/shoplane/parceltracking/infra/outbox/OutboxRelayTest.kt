package com.shoplane.parceltracking.infra.outbox

import com.shoplane.parceltracking.fixtures.fixedClockAt
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.time.Instant
import java.util.concurrent.CompletableFuture

class OutboxRelayTest {
    private val outbox = mockk<OutboxMessageRepository>()
    private val kafkaTemplate = mockk<KafkaTemplate<String, String>>()
    private val relay = OutboxRelay(outbox, kafkaTemplate, fixedClockAt("2026-11-04T12:00:01Z"))

    @Test
    fun `sends pending messages and marks them published`() {
        val message = aMessage()
        every { outbox.findTop100ByPublishedAtIsNullOrderByIdAsc() } returns listOf(message)
        every { kafkaTemplate.send(any<ProducerRecord<String, String>>()) } returns
            CompletableFuture.completedFuture(mockk<SendResult<String, String>>())

        relay.relayPendingMessages()

        assertThat(message.publishedAt).isEqualTo(Instant.parse("2026-11-04T12:00:01Z"))
        verify { kafkaTemplate.send(match<ProducerRecord<String, String>> { it.key() == message.messageKey }) }
    }

    @Test
    fun `a failed send leaves the message pending`() {
        val message = aMessage()
        every { outbox.findTop100ByPublishedAtIsNullOrderByIdAsc() } returns listOf(message)
        every { kafkaTemplate.send(any<ProducerRecord<String, String>>()) } returns
            CompletableFuture.failedFuture(IllegalStateException("Broker unavailable"))

        assertThatThrownBy { relay.relayPendingMessages() }.hasRootCauseMessage("Broker unavailable")
        assertThat(message.publishedAt).isNull()
    }

    private fun aMessage() =
        OutboxMessageEntity(
            topic = "shoplane.parcel-tracking.parcel-events.v1",
            messageKey = "0f8b2c1e-6d3a-4c55-9b1e-2a7f4d9c8e11",
            eventType = "parcel.status-changed",
            payload = """{"currentStatus":"DELIVERED"}""",
            createdAt = Instant.parse("2026-11-04T12:00:00Z"),
        )
}
