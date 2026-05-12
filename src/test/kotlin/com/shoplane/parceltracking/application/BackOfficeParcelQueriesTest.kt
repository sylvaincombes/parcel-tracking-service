package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.parcel.PageRequest
import com.shoplane.parceltracking.fixtures.GARDEN_TOOLS_SELLER
import com.shoplane.parceltracking.fixtures.InMemoryParcelRepository
import com.shoplane.parceltracking.fixtures.LIGHTING_SELLER
import com.shoplane.parceltracking.fixtures.aRegisteredParcel
import com.shoplane.parceltracking.fixtures.aSellerAdmin
import com.shoplane.parceltracking.fixtures.aSellerViewer
import com.shoplane.parceltracking.fixtures.aSupportAgent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class BackOfficeParcelQueriesTest {
    private val gardenParcel = aRegisteredParcel(trackingNumber = "CW4400112233FR", sellerId = GARDEN_TOOLS_SELLER)
    private val lightingParcel = aRegisteredParcel(trackingNumber = "CW4400998877FR", sellerId = LIGHTING_SELLER)
    private val queries = BackOfficeParcelQueries(InMemoryParcelRepository(gardenParcel, lightingParcel), ParcelAccessPolicy())

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
}
