package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.parcel.ParcelStatus
import com.shoplane.parceltracking.fixtures.InMemoryParcelRepository
import com.shoplane.parceltracking.fixtures.LIGHTING_SELLER
import com.shoplane.parceltracking.fixtures.RecordingParcelEventPublisher
import com.shoplane.parceltracking.fixtures.aRegisteredParcel
import com.shoplane.parceltracking.fixtures.aSellerAdmin
import com.shoplane.parceltracking.fixtures.aSupportAgent
import com.shoplane.parceltracking.fixtures.fixedClockAt
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class CancelParcelUseCaseTest {
    private val parcel = aRegisteredParcel()
    private val parcels = InMemoryParcelRepository(parcel)
    private val publisher = RecordingParcelEventPublisher()
    private val useCase = CancelParcelUseCase(parcels, ParcelAccessPolicy(), publisher, fixedClockAt("2026-11-02T10:00:00Z"))

    @Test
    fun `the seller cancels its own parcel`() {
        val cancelled = useCase.cancel(aSellerAdmin(), parcel.id)

        assertThat(cancelled.status).isEqualTo(ParcelStatus.CANCELLED)
        assertThat(publisher.published).hasSize(1)
    }

    @Test
    fun `another seller cannot cancel the parcel`() {
        assertThatThrownBy { useCase.cancel(aSellerAdmin(LIGHTING_SELLER), parcel.id) }
            .isInstanceOf(AccessDeniedException::class.java)
        assertThat(parcels.findById(parcel.id)?.status).isEqualTo(ParcelStatus.REGISTERED)
        assertThat(publisher.published).isEmpty()
    }

    @Test
    fun `support agents can cancel any parcel`() {
        assertThat(useCase.cancel(aSupportAgent(), parcel.id).status).isEqualTo(ParcelStatus.CANCELLED)
    }
}
