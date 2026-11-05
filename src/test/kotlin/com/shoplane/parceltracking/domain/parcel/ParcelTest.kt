package com.shoplane.parceltracking.domain.parcel

import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.fixtures.TrackingEvents
import com.shoplane.parceltracking.fixtures.aPickedUpParcel
import com.shoplane.parceltracking.fixtures.aRegisteredParcel
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class ParcelTest {
    private val now = Instant.parse("2026-11-04T10:00:00Z")

    @Test
    fun `registration drops blank delivery instructions`() {
        val parcel = Parcel.register(
            trackingNumber = TrackingNumber.of("CW4400112233FR"),
            carrierCode = CarrierCode("colisway"),
            sellerId = SellerId(4101),
            orderReference = OrderReference("SO-2026-118734"),
            deliveryInstructions = "   ",
            now = now,
        )

        assertThat(parcel.status).isEqualTo(ParcelStatus.REGISTERED)
        assertThat(parcel.deliveryInstructions).isNull()
    }

    @Test
    fun `pick-up records the pick-up day and reports the status change`() {
        val parcel = aRegisteredParcel()

        val change = parcel.record(TrackingEvents.pickedUp("2026-11-02T14:00:00Z"), now)

        assertThat(parcel.pickedUpOn).isEqualTo(LocalDate.parse("2026-11-02"))
        assertThat(change?.previousStatus).isEqualTo(ParcelStatus.REGISTERED)
        assertThat(change?.currentStatus).isEqualTo(ParcelStatus.PICKED_UP)
    }

    @Test
    fun `delivery stores the carrier delivery time`() {
        val parcel = aPickedUpParcel()
        parcel.record(TrackingEvents.outForDelivery("2026-11-04T06:10:00Z"), now)

        parcel.record(TrackingEvents.delivered("2026-11-04T11:42:00Z"), now)

        assertThat(parcel.status).isEqualTo(ParcelStatus.DELIVERED)
        assertThat(parcel.deliveredAt).isEqualTo(Instant.parse("2026-11-04T11:42:00Z"))
    }

    @Test
    fun `another hub scan keeps the parcel in transit without reporting a status change`() {
        val parcel = aPickedUpParcel()
        parcel.record(TrackingEvents.inTransit("2026-11-03T02:00:00Z"), now)

        val change = parcel.record(TrackingEvents.arrivedAtHub("2026-11-03T09:00:00Z", location = "Marseille hub"), now)

        assertThat(change).isNull()
        assertThat(parcel.events).hasSize(3)
    }

    @Test
    fun `rejects an event that would reopen a delivered parcel`() {
        val parcel = aPickedUpParcel()
        parcel.record(TrackingEvents.outForDelivery("2026-11-04T06:10:00Z"), now)
        parcel.record(TrackingEvents.delivered("2026-11-04T11:42:00Z"), now)

        assertThatThrownBy { parcel.record(TrackingEvents.inTransit("2026-11-04T12:00:00Z"), now) }
            .isInstanceOf(IllegalStatusTransitionException::class.java)
        assertThat(parcel.status).isEqualTo(ParcelStatus.DELIVERED)
    }

    @Test
    fun `a parcel already handed to the carrier cannot be cancelled`() {
        val parcel = aPickedUpParcel()

        assertThatThrownBy { parcel.cancel(now) }.isInstanceOf(IllegalStatusTransitionException::class.java)
    }

    @Test
    fun `timeline is ordered by carrier event time, not by arrival`() {
        val parcel = aPickedUpParcel(pickedUpAt = "2026-11-02T14:00:00Z")
        parcel.record(TrackingEvents.outForDelivery("2026-11-04T06:10:00Z"), now)
        parcel.record(TrackingEvents.inTransit("2026-11-03T02:00:00Z"), now)

        assertThat(parcel.timeline().map { it.type }).containsExactly(
            TrackingEventType.PICKED_UP,
            TrackingEventType.IN_TRANSIT,
            TrackingEventType.OUT_FOR_DELIVERY,
        )
    }

    @Test
    fun `late detection keeps the first detection time`() {
        val parcel = aPickedUpParcel()
        parcel.markLate(now)

        parcel.markLate(now.plusSeconds(3600))

        assertThat(parcel.lateDetectedAt).isEqualTo(now)
    }

    @Test
    fun `a closed parcel cannot be flagged late`() {
        val parcel = aRegisteredParcel()
        parcel.cancel(now)

        assertThatIllegalStateException().isThrownBy { parcel.markLate(now) }
    }
}
