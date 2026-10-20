package com.shoplane.parceltracking.domain.lateness

import com.shoplane.parceltracking.domain.calendar.BusinessCalendar
import com.shoplane.parceltracking.domain.carrier.TransitDays
import com.shoplane.parceltracking.domain.parcel.TrackingEventType
import com.shoplane.parceltracking.fixtures.aCarrier
import com.shoplane.parceltracking.fixtures.aPickedUpParcel
import com.shoplane.parceltracking.fixtures.aRegisteredParcel
import com.shoplane.parceltracking.fixtures.event
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class LateParcelDetectorTest {
    private val detector = LateParcelDetector(BusinessCalendar(emptySet()), graceBusinessDays = 1)
    private val carrier = aCarrier(transitDays = TransitDays(min = 1, max = 2))

    @Test
    fun `a parcel still in transit a week after pick-up is late`() {
        val parcel = aPickedUpParcel(pickedUpAt = "2026-11-16T14:00:00Z")

        assertThat(detector.isLate(parcel, carrier, LocalDate.parse("2026-11-23"))).isTrue()
    }

    @Test
    fun `a parcel picked up yesterday is not late`() {
        val parcel = aPickedUpParcel(pickedUpAt = "2026-11-16T14:00:00Z")

        assertThat(detector.isLate(parcel, carrier, LocalDate.parse("2026-11-17"))).isFalse()
    }

    @Test
    fun `a parcel never picked up is not late`() {
        assertThat(detector.isLate(aRegisteredParcel(), carrier, LocalDate.parse("2026-11-30"))).isFalse()
    }

    @Test
    fun `a delivered parcel is never late`() {
        val parcel = aPickedUpParcel(pickedUpAt = "2026-11-02T14:00:00Z").apply {
            record(event(TrackingEventType.OUT_FOR_DELIVERY, "2026-11-12T06:00:00Z"), Instant.parse("2026-11-12T06:00:00Z"))
            record(event(TrackingEventType.DELIVERED, "2026-11-12T10:00:00Z"), Instant.parse("2026-11-12T10:00:00Z"))
        }

        assertThat(detector.isLate(parcel, carrier, LocalDate.parse("2026-11-30"))).isFalse()
    }

    @Test
    fun `a parcel already flagged late is not reported twice`() {
        val parcel = aPickedUpParcel(pickedUpAt = "2026-11-02T14:00:00Z").apply {
            markLate(Instant.parse("2026-11-09T15:00:00Z"))
        }

        assertThat(detector.isLate(parcel, carrier, LocalDate.parse("2026-11-30"))).isFalse()
    }
}
