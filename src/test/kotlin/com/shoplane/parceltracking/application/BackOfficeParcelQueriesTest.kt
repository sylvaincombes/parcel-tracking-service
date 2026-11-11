package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.parcel.PageRequest
import com.shoplane.parceltracking.domain.parcel.TrackingEventType
import com.shoplane.parceltracking.fixtures.GARDEN_TOOLS_SELLER
import com.shoplane.parceltracking.fixtures.InMemoryParcelRepository
import com.shoplane.parceltracking.fixtures.LIGHTING_SELLER
import com.shoplane.parceltracking.fixtures.aPickedUpParcel
import com.shoplane.parceltracking.fixtures.aRegisteredParcel
import com.shoplane.parceltracking.fixtures.aSellerAdmin
import com.shoplane.parceltracking.fixtures.aSellerViewer
import com.shoplane.parceltracking.fixtures.aSupportAgent
import com.shoplane.parceltracking.fixtures.event
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant

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
    fun `a seller reads the tracking events of its parcel`() {
        val parcel = aPickedUpParcel(trackingNumber = "CW4400554433FR", pickedUpAt = "2026-11-02T14:00:00Z").apply {
            record(event(TrackingEventType.IN_TRANSIT, "2026-11-03T02:00:00Z"), Instant.parse("2026-11-03T02:05:00Z"))
        }
        val queries = BackOfficeParcelQueries(InMemoryParcelRepository(parcel), ParcelAccessPolicy())

        val events = queries.events(aSellerViewer(), parcel.id)

        assertThat(events.map { it.type }).containsExactly(TrackingEventType.PICKED_UP, TrackingEventType.IN_TRANSIT)
    }

    @Test
    fun `a seller cannot read the tracking events of another seller's parcel`() {
        assertThatThrownBy { queries.events(aSellerAdmin(GARDEN_TOOLS_SELLER), lightingParcel.id) }
            .isInstanceOf(AccessDeniedException::class.java)
    }

    @Test
    fun `a seller cannot read the detail of another seller's parcel`() {
        assertThatThrownBy { queries.detail(aSellerAdmin(GARDEN_TOOLS_SELLER), lightingParcel.id) }
            .isInstanceOf(AccessDeniedException::class.java)
    }
}
