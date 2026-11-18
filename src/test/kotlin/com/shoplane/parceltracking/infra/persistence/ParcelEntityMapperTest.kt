package com.shoplane.parceltracking.infra.persistence

import com.shoplane.parceltracking.domain.parcel.ParcelStatus
import com.shoplane.parceltracking.domain.parcel.TrackingEventType
import com.shoplane.parceltracking.fixtures.aPickedUpParcel
import com.shoplane.parceltracking.fixtures.event
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class ParcelEntityMapperTest {
    private val now = Instant.parse("2026-11-04T12:00:00Z")

    @Test
    fun `a parcel survives a round trip through its entity`() {
        val parcel = aPickedUpParcel()

        val restored = ParcelEntityMapper.toDomain(ParcelEntityMapper.toNewEntity(parcel, now))

        assertThat(restored.id).isEqualTo(parcel.id)
        assertThat(restored.status).isEqualTo(ParcelStatus.PICKED_UP)
        assertThat(restored.pickedUpOn).isEqualTo(parcel.pickedUpOn)
        assertThat(restored.events).isEqualTo(parcel.events)
    }

    @Test
    fun `a returned parcel keeps its return reason through its entity`() {
        val parcel = aPickedUpParcel().apply {
            record(event(TrackingEventType.IN_TRANSIT, "2026-11-03T02:00:00Z"), now)
            record(event(TrackingEventType.RETURNED_TO_SENDER, "2026-11-04T09:30:00Z"), now)
            recordReturnReason("Address unknown, returning to sender")
        }

        val restored = ParcelEntityMapper.toDomain(ParcelEntityMapper.toNewEntity(parcel, now))

        assertThat(restored.status).isEqualTo(ParcelStatus.RETURNED_TO_SENDER)
        assertThat(restored.returnReason).isEqualTo("Address unknown, returning to sender")
    }

    @Test
    fun `applying changes only appends events not yet persisted`() {
        val parcel = aPickedUpParcel()
        val entity = ParcelEntityMapper.toNewEntity(parcel, now)
        parcel.record(event(TrackingEventType.IN_TRANSIT, "2026-11-03T02:00:00Z"), now)

        ParcelEntityMapper.applyChanges(entity, parcel, now)

        assertThat(entity.events.map { it.type }).containsExactly(TrackingEventType.PICKED_UP, TrackingEventType.IN_TRANSIT)
        assertThat(entity.status).isEqualTo(ParcelStatus.IN_TRANSIT)
    }
}
