package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.fixtures.InMemoryParcelRepository
import com.shoplane.parceltracking.fixtures.LIGHTING_SELLER
import com.shoplane.parceltracking.fixtures.aPickedUpParcel
import com.shoplane.parceltracking.fixtures.aSellerAdmin
import com.shoplane.parceltracking.fixtures.aSellerOperator
import com.shoplane.parceltracking.fixtures.aSellerViewer
import com.shoplane.parceltracking.fixtures.aSupportAgent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class UpdateSellerNoteUseCaseTest {
    private val parcel = aPickedUpParcel()
    private val parcels = InMemoryParcelRepository(parcel)
    private val useCase = UpdateSellerNoteUseCase(parcels, ParcelAccessPolicy())

    @Test
    fun `the seller leaves a note for support on its parcel`() {
        val updated = useCase.update(aSellerOperator(), parcel.id, "  Glass lamp shade, customer was promised Thursday  ")

        assertThat(updated.sellerNote).isEqualTo("Glass lamp shade, customer was promised Thursday")
        assertThat(parcels.findById(parcel.id)?.sellerNote).isEqualTo("Glass lamp shade, customer was promised Thursday")
    }

    @Test
    fun `a blank note clears the previous one`() {
        useCase.update(aSellerOperator(), parcel.id, "Customer asked for a call before delivery")

        useCase.update(aSellerOperator(), parcel.id, "   ")

        assertThat(parcels.findById(parcel.id)?.sellerNote).isNull()
    }

    @Test
    fun `support agents can change the note of any parcel`() {
        useCase.update(aSupportAgent(), parcel.id, "Replacement sent under CW4400998877FR")

        assertThat(parcels.findById(parcel.id)?.sellerNote).isEqualTo("Replacement sent under CW4400998877FR")
    }

    @Test
    fun `another seller cannot leave a note on the parcel`() {
        assertThatThrownBy { useCase.update(aSellerAdmin(LIGHTING_SELLER), parcel.id, "Customer asked for a call before delivery") }
            .isInstanceOf(AccessDeniedException::class.java)
        assertThat(parcels.findById(parcel.id)?.sellerNote).isNull()
    }

    @Test
    fun `read-only seller users cannot leave a note`() {
        assertThatThrownBy { useCase.update(aSellerViewer(), parcel.id, "Customer asked for a call before delivery") }
            .isInstanceOf(AccessDeniedException::class.java)
    }

    @Test
    fun `an unknown parcel is not found`() {
        assertThatThrownBy { useCase.update(aSellerOperator(), ParcelId.generate(), "Customer asked for a call before delivery") }
            .isInstanceOf(ParcelNotFoundException::class.java)
    }
}
