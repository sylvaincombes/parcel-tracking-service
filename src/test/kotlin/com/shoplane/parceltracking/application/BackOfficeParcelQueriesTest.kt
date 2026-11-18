package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.calendar.BusinessCalendar
import com.shoplane.parceltracking.domain.parcel.PageRequest
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.TrackingEventType
import com.shoplane.parceltracking.domain.transit.TransitTimeCalculator
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
    private val queries = queriesOver(gardenParcel, lightingParcel)

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
    fun `the detail of a delivered parcel shows the business days it spent in transit`() {
        val deliveredParcel = aPickedUpParcel(pickedUpAt = "2026-11-16T14:00:00Z").apply {
            record(event(TrackingEventType.OUT_FOR_DELIVERY, "2026-11-18T06:00:00Z"), Instant.parse("2026-11-18T06:00:00Z"))
            record(event(TrackingEventType.DELIVERED, "2026-11-18T10:00:00Z"), Instant.parse("2026-11-18T10:00:00Z"))
        }

        val detail = queriesOver(deliveredParcel).detail(aSellerViewer(), deliveredParcel.id)

        assertThat(detail.transitBusinessDays).isEqualTo(2)
    }

    @Test
    fun `the detail of a parcel not delivered yet has no transit time`() {
        val detail = queries.detail(aSellerViewer(), gardenParcel.id)

        assertThat(detail.transitBusinessDays).isNull()
    }

    @Test
    fun `a seller cannot read the detail of another seller's parcel`() {
        assertThatThrownBy { queries.detail(aSellerAdmin(GARDEN_TOOLS_SELLER), lightingParcel.id) }
            .isInstanceOf(AccessDeniedException::class.java)
    }

    private fun queriesOver(vararg parcels: Parcel) =
        BackOfficeParcelQueries(
            InMemoryParcelRepository(*parcels),
            ParcelAccessPolicy(),
            TransitTimeCalculator(BusinessCalendar(emptySet())),
        )
}
