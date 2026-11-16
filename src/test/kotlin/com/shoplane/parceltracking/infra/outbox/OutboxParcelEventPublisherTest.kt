package com.shoplane.parceltracking.infra.outbox

import com.shoplane.parceltracking.domain.event.ParcelStatusChanged
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelStatus
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import com.shoplane.parceltracking.fixtures.COLISWAY
import com.shoplane.parceltracking.fixtures.GARDEN_TOOLS_SELLER
import com.shoplane.parceltracking.fixtures.fixedClockAt
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.time.Instant
import java.util.UUID

class OutboxParcelEventPublisherTest {
    private val outbox = mockk<OutboxMessageRepository>()
    private val publisher = OutboxParcelEventPublisher(
        outbox = outbox,
        jsonMapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build(),
        topics = OutboxTopics(parcelEvents = "shoplane.parcel-tracking.parcel-events.v1"),
        clock = fixedClockAt("2026-11-04T12:00:00Z"),
    )

    @Test
    fun `stores the event in the outbox keyed by parcel`() {
        val stored = slot<OutboxMessageEntity>()
        every { outbox.save(capture(stored)) } answers { firstArg() }
        val parcelId = ParcelId(UUID.fromString("0f8b2c1e-6d3a-4c55-9b1e-2a7f4d9c8e11"))

        publisher.publish(
            ParcelStatusChanged(
                parcelId = parcelId,
                trackingNumber = TrackingNumber.of("CW4400112233FR"),
                carrierCode = COLISWAY,
                sellerId = GARDEN_TOOLS_SELLER,
                previousStatus = ParcelStatus.OUT_FOR_DELIVERY,
                currentStatus = ParcelStatus.DELIVERED,
                occurredAt = Instant.parse("2026-11-04T11:42:00Z"),
            ),
        )

        assertThat(stored.captured.topic).isEqualTo("shoplane.parcel-tracking.parcel-events.v1")
        assertThat(stored.captured.messageKey).isEqualTo(parcelId.toString())
        assertThat(stored.captured.eventType).isEqualTo("parcel.status-changed")
        assertThat(stored.captured.payload)
            .contains("\"currentStatus\":\"DELIVERED\"")
            .contains("\"trackingNumber\":\"CW4400112233FR\"")
            .contains("\"carrier\":\"colisway\"")
        assertThat(stored.captured.publishedAt).isNull()
    }
}
