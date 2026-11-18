package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.carrier.Carrier
import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.carrier.CarrierDirectory
import com.shoplane.parceltracking.domain.parcel.PageRequest
import com.shoplane.parceltracking.fixtures.COLISWAY
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

class BackOfficeParcelQueriesTest {
    private val gardenParcel = aRegisteredParcel(trackingNumber = "CW4400112233FR", sellerId = GARDEN_TOOLS_SELLER)
    private val lightingParcel = aRegisteredParcel(trackingNumber = "CW4400998877FR", sellerId = LIGHTING_SELLER)
    private val queries = BackOfficeParcelQueries(
        InMemoryParcelRepository(gardenParcel, lightingParcel),
        ParcelAccessPolicy(),
        StubCarrierDirectory(aCarrier(code = COLISWAY, displayName = "Colisway")),
    )

    @Test
    fun `a seller only lists its own parcels`() {
        val listed = queries.list(aSellerViewer(), null, PageRequest(0, 50))

        assertThat(listed.map { it.parcel.id }).containsExactly(gardenParcel.id)
    }

    @Test
    fun `a support agent lists the parcels of the requested seller`() {
        val listed = queries.list(aSupportAgent(), LIGHTING_SELLER, PageRequest(0, 50))

        assertThat(listed.map { it.parcel.id }).containsExactly(lightingParcel.id)
    }

    @Test
    fun `listed parcels show the carrier name, or none when the registry does not know the carrier`() {
        val unknownCarrierParcel = aRegisteredParcel(trackingNumber = "PW7781200456FR", carrierCode = PARCELWAVE)
        val queries = BackOfficeParcelQueries(
            InMemoryParcelRepository(gardenParcel, unknownCarrierParcel),
            ParcelAccessPolicy(),
            StubCarrierDirectory(aCarrier(code = COLISWAY, displayName = "Colisway")),
        )

        val names = queries.list(aSellerViewer(), null, PageRequest(0, 50)).associate { it.parcel.id to it.carrierName }

        assertThat(names).containsEntry(gardenParcel.id, "Colisway").containsEntry(unknownCarrierParcel.id, null)
    }

    @Test
    fun `each carrier is looked up once per page`() {
        val directory = CountingCarrierDirectory(aCarrier(code = COLISWAY))
        val queries = BackOfficeParcelQueries(
            InMemoryParcelRepository(
                aRegisteredParcel(trackingNumber = "CW4400112233FR"),
                aRegisteredParcel(trackingNumber = "CW4400112244FR"),
                aRegisteredParcel(trackingNumber = "CW4400112255FR"),
            ),
            ParcelAccessPolicy(),
            directory,
        )

        queries.list(aSellerViewer(), null, PageRequest(0, 50))

        assertThat(directory.lookups).isEqualTo(1)
    }

    @Test
    fun `a seller cannot read the detail of another seller's parcel`() {
        assertThatThrownBy { queries.detail(aSellerAdmin(GARDEN_TOOLS_SELLER), lightingParcel.id) }
            .isInstanceOf(AccessDeniedException::class.java)
    }

    private class CountingCarrierDirectory(vararg carriers: Carrier) : CarrierDirectory {
        private val delegate = StubCarrierDirectory(*carriers)
        var lookups = 0
            private set

        override fun findCarrier(code: CarrierCode): Carrier? {
            lookups++
            return delegate.findCarrier(code)
        }
    }
}
