package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.domain.parcel.DeliveryInstructionsLockedException
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.TrackingEventType
import com.shoplane.parceltracking.fixtures.InMemoryParcelRepository
import com.shoplane.parceltracking.fixtures.aPickedUpParcel
import com.shoplane.parceltracking.fixtures.aRegisteredParcel
import com.shoplane.parceltracking.fixtures.aSellerOperator
import com.shoplane.parceltracking.fixtures.event
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant

class UpdateDeliveryInstructionsUseCaseTest {
    @Test
    fun `the seller updates the delivery instructions of a parcel in transit`() {
        val parcel = aPickedUpParcel()
        val parcels = InMemoryParcelRepository(parcel)

        UpdateDeliveryInstructionsUseCase(parcels).update(aSellerOperator(), parcel.id, "  Back door, code 4521  ")

        assertThat(parcels.findById(parcel.id)?.deliveryInstructions).isEqualTo("Back door, code 4521")
    }

    @Test
    fun `blank instructions clear the previous ones`() {
        val parcel = aRegisteredParcel(deliveryInstructions = "Leave at the garden gate")
        val parcels = InMemoryParcelRepository(parcel)

        UpdateDeliveryInstructionsUseCase(parcels).update(aSellerOperator(), parcel.id, " ")

        assertThat(parcels.findById(parcel.id)?.deliveryInstructions).isNull()
    }

    @Test
    fun `instructions cannot change once the parcel is out for delivery`() {
        val parcel = aPickedUpParcel().apply {
            record(event(TrackingEventType.OUT_FOR_DELIVERY, "2026-11-04T06:10:00Z"), Instant.parse("2026-11-04T06:10:00Z"))
        }
        val parcels = InMemoryParcelRepository(parcel)

        assertThatThrownBy { UpdateDeliveryInstructionsUseCase(parcels).update(aSellerOperator(), parcel.id, "Ring twice") }
            .isInstanceOf(DeliveryInstructionsLockedException::class.java)
    }

    @Test
    fun `an unknown parcel is not found`() {
        assertThatThrownBy {
            UpdateDeliveryInstructionsUseCase(InMemoryParcelRepository()).update(aSellerOperator(), ParcelId.generate(), "Ring twice")
        }.isInstanceOf(ParcelNotFoundException::class.java)
    }
}
