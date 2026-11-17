package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.parcel.PageRequest
import com.shoplane.parceltracking.fixtures.GARDEN_TOOLS_SELLER
import com.shoplane.parceltracking.fixtures.InMemoryParcelRepository
import com.shoplane.parceltracking.fixtures.LIGHTING_SELLER
import com.shoplane.parceltracking.fixtures.PARCELWAVE
import com.shoplane.parceltracking.fixtures.StubCarrierDirectory
import com.shoplane.parceltracking.fixtures.aCarrier
import com.shoplane.parceltracking.fixtures.aRegisteredParcel
import com.shoplane.parceltracking.fixtures.aSellerAdmin
import com.shoplane.parceltracking.fixtures.aSellerViewer
import com.shoplane.parceltracking.fixtures.aSupportAgent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.net.URI

class BackOfficeParcelQueriesTest {
    private val gardenParcel = aRegisteredParcel(trackingNumber = "CW4400112233FR", sellerId = GARDEN_TOOLS_SELLER)
    private val lightingParcel = aRegisteredParcel(trackingNumber = "CW4400998877FR", sellerId = LIGHTING_SELLER)
    private val queries = BackOfficeParcelQueries(
        InMemoryParcelRepository(gardenParcel, lightingParcel),
        ParcelAccessPolicy(),
        StubCarrierDirectory(aCarrier()),
    )

    @Test
    fun `a seller only lists its own parcels`() {
        val listed = queries.list(aSellerViewer(), null, PageRequest(0, 50))

        assertThat(listed.map { it.id }).containsExactly(gardenParcel.id)
    }

    @Test
    fun `a support agent lists the parcels of the requested seller`() {
        val listed = queries.list(aSupportAgent(), LIGHTING_SELLER, PageRequest(0, 50))

        assertThat(listed.map { it.id }).containsExactly(lightingParcel.id)
    }

    @Test
    fun `a seller cannot read the detail of another seller's parcel`() {
        assertThatThrownBy { queries.detail(aSellerAdmin(GARDEN_TOOLS_SELLER), lightingParcel.id) }
            .isInstanceOf(AccessDeniedException::class.java)
    }

    @Test
    fun `detail`() {
        val detail = queries.detail(aSellerViewer(), gardenParcel.id)

        assertThat(detail.carrierTrackingLink).isEqualTo(URI.create("https://track.colisway.example/parcels/CW4400112233FR"))
    }

    @Test
    fun `detail 2`() {
        val parcel = aRegisteredParcel(trackingNumber = "PW7781200456FR", carrierCode = PARCELWAVE)
        val queries = BackOfficeParcelQueries(InMemoryParcelRepository(parcel), ParcelAccessPolicy(), StubCarrierDirectory())

        val detail = queries.detail(aSupportAgent(), parcel.id)

        assertThat(detail.parcel.id).isEqualTo(parcel.id)
        assertThat(detail.carrierTrackingLink).isNull()
    }
}
