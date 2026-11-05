package com.shoplane.parceltracking.presentation.publicapi

import com.shoplane.parceltracking.application.PublicTracking
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.fixtures.aRegisteredParcel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PublicTrackingResponseTest {
    @Test
    fun `shows the delivery instructions the seller gave to the carrier`() {
        val parcel = aRegisteredParcel(deliveryInstructions = "Leave at the back door")

        val response = PublicTrackingResponse.from(aTracking(parcel))

        assertThat(response.deliveryInstructions).isEqualTo("Leave at the back door")
    }

    @Test
    fun `a parcel without delivery instructions shows none`() {
        val response = PublicTrackingResponse.from(aTracking(aRegisteredParcel()))

        assertThat(response.deliveryInstructions).isNull()
    }

    private fun aTracking(parcel: Parcel) =
        PublicTracking(parcel = parcel, carrierName = "Colisway", carrierTrackingLink = null, estimate = null)
}
