package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.parcel.OrderReference
import com.shoplane.parceltracking.fixtures.InMemoryParcelRepository
import com.shoplane.parceltracking.fixtures.aRegisteredParcel
import com.shoplane.parceltracking.fixtures.aSellerOperator
import com.shoplane.parceltracking.fixtures.aSellerViewer
import com.shoplane.parceltracking.fixtures.aSupportAgent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class UpdateOrderReferenceUseCaseTest {
    private val parcel = aRegisteredParcel(orderReference = "SO-2026-118734")
    private val parcels = InMemoryParcelRepository(parcel)
    private val useCase = UpdateOrderReferenceUseCase(parcels, ParcelAccessPolicy())

    @Test
    fun `the seller fixes the order reference of its parcel`() {
        useCase.update(aSellerOperator(), parcel.id, OrderReference("SO-2026-118743"))

        assertThat(parcels.findById(parcel.id)?.orderReference).isEqualTo(OrderReference("SO-2026-118743"))
    }

    @Test
    fun `read-only users of the seller cannot change the order reference`() {
        assertThatThrownBy { useCase.update(aSellerViewer(), parcel.id, OrderReference("SO-2026-118743")) }
            .isInstanceOf(AccessDeniedException::class.java)
        assertThat(parcels.findById(parcel.id)?.orderReference).isEqualTo(OrderReference("SO-2026-118734"))
    }

    @Test
    fun `support agents can change the order reference of any parcel`() {
        useCase.update(aSupportAgent(), parcel.id, OrderReference("SO-2026-118743"))

        assertThat(parcels.findById(parcel.id)?.orderReference).isEqualTo(OrderReference("SO-2026-118743"))
    }
}
