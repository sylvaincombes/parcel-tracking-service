package com.shoplane.parceltracking.domain.transit

import com.shoplane.parceltracking.domain.calendar.BusinessCalendar
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.TrackingEventType
import com.shoplane.parceltracking.fixtures.aPickedUpParcel
import com.shoplane.parceltracking.fixtures.event
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class TransitTimeCalculatorTest {
    private val calculator = TransitTimeCalculator(BusinessCalendar(setOf(LocalDate.parse("2026-11-11"))))

    @Test
    fun `counts the business days between pick-up and delivery`() {
        val parcel = aPickedUpParcel(pickedUpAt = "2026-11-16T14:00:00Z").deliveredAt("2026-11-18T10:00:00Z")

        assertThat(calculator.businessDaysInTransit(parcel)).isEqualTo(2)
    }

    @Test
    fun `weekends and public holidays do not count as days in transit`() {
        val parcel = aPickedUpParcel(pickedUpAt = "2026-11-10T14:00:00Z").deliveredAt("2026-11-16T10:00:00Z")

        assertThat(calculator.businessDaysInTransit(parcel)).isEqualTo(3)
    }

    @Test
    fun `a parcel delivered on its pick-up day spent no business day in transit`() {
        val parcel = aPickedUpParcel(pickedUpAt = "2026-11-16T07:00:00Z").deliveredAt("2026-11-16T17:00:00Z")

        assertThat(calculator.businessDaysInTransit(parcel)).isZero()
    }

    @Test
    fun `a parcel not delivered yet has no transit time`() {
        assertThat(calculator.businessDaysInTransit(aPickedUpParcel())).isNull()
    }

    private fun Parcel.deliveredAt(deliveredAt: String): Parcel =
        apply {
            record(event(TrackingEventType.OUT_FOR_DELIVERY, deliveredAt), Instant.parse(deliveredAt))
            record(event(TrackingEventType.DELIVERED, deliveredAt), Instant.parse(deliveredAt))
        }
}
