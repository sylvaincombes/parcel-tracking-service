package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import com.shoplane.parceltracking.fixtures.GARDEN_TOOLS_SELLER
import com.shoplane.parceltracking.fixtures.InMemoryParcelRepository
import com.shoplane.parceltracking.fixtures.LIGHTING_SELLER
import com.shoplane.parceltracking.fixtures.aRegisteredParcel
import com.shoplane.parceltracking.fixtures.aSellerViewer
import com.shoplane.parceltracking.fixtures.aSupportAgent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class FindParcelByTrackingNumberUseCaseTest {
    private val gardenParcel = aRegisteredParcel(trackingNumber = "CW4400112233FR", sellerId = GARDEN_TOOLS_SELLER)
    private val lightingParcel = aRegisteredParcel(trackingNumber = "CW4400998877FR", sellerId = LIGHTING_SELLER)
    private val useCase = FindParcelByTrackingNumberUseCase(
        InMemoryParcelRepository(gardenParcel, lightingParcel),
        ParcelAccessPolicy(),
    )

    @Test
    fun `finds the parcel from a tracking number pasted from a customer email`() {
        val found = useCase.find(aSellerViewer(GARDEN_TOOLS_SELLER), TrackingNumber.of("cw44 0011 2233 fr"))

        assertThat(found.id).isEqualTo(gardenParcel.id)
    }

    @Test
    fun `a support agent finds the parcel of any seller`() {
        val found = useCase.find(aSupportAgent(), TrackingNumber.of("CW4400998877FR"))

        assertThat(found.id).isEqualTo(lightingParcel.id)
    }

    @Test
    fun `a seller cannot look up another seller's parcel`() {
        assertThatThrownBy { useCase.find(aSellerViewer(GARDEN_TOOLS_SELLER), TrackingNumber.of("CW4400998877FR")) }
            .isInstanceOf(AccessDeniedException::class.java)
    }

    @Test
    fun `an unknown tracking number is not found`() {
        assertThatThrownBy { useCase.find(aSupportAgent(), TrackingNumber.of("CW4499999999FR")) }
            .isInstanceOf(ParcelNotFoundException::class.java)
    }
}
