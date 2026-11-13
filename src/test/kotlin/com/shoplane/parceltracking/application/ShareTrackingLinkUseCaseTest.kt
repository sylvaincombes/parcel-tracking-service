package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.PublicTrackingPage
import com.shoplane.parceltracking.fixtures.InMemoryParcelRepository
import com.shoplane.parceltracking.fixtures.LIGHTING_SELLER
import com.shoplane.parceltracking.fixtures.aRegisteredParcel
import com.shoplane.parceltracking.fixtures.aSellerAdmin
import com.shoplane.parceltracking.fixtures.aSellerViewer
import com.shoplane.parceltracking.fixtures.aSupportAgent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.net.URI

class ShareTrackingLinkUseCaseTest {
    private val parcel = aRegisteredParcel(trackingNumber = "CW4400112233FR")
    private val useCase = ShareTrackingLinkUseCase(
        InMemoryParcelRepository(parcel),
        ParcelAccessPolicy(),
        PublicTrackingPage("https://www.shoplane.example/tracking"),
    )

    @Test
    fun `the seller gets the public tracking link of its parcel`() {
        assertThat(useCase.linkFor(aSellerViewer(), parcel.id))
            .isEqualTo(URI.create("https://www.shoplane.example/tracking/CW4400112233FR"))
    }

    @Test
    fun `support agents get the link of any parcel`() {
        assertThat(useCase.linkFor(aSupportAgent(), parcel.id))
            .isEqualTo(URI.create("https://www.shoplane.example/tracking/CW4400112233FR"))
    }

    @Test
    fun `another seller cannot get the link of the parcel`() {
        assertThatThrownBy { useCase.linkFor(aSellerAdmin(LIGHTING_SELLER), parcel.id) }
            .isInstanceOf(AccessDeniedException::class.java)
    }

    @Test
    fun `an unknown parcel is not found`() {
        assertThatThrownBy { useCase.linkFor(aSellerAdmin(), ParcelId.generate()) }
            .isInstanceOf(ParcelNotFoundException::class.java)
    }
}
