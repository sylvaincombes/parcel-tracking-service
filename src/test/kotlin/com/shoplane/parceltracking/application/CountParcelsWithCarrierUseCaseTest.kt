package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.fixtures.GARDEN_TOOLS_SELLER
import com.shoplane.parceltracking.fixtures.LIGHTING_SELLER
import com.shoplane.parceltracking.fixtures.StubParcelStatistics
import com.shoplane.parceltracking.fixtures.aSellerViewer
import com.shoplane.parceltracking.fixtures.aSupportAgent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class CountParcelsWithCarrierUseCaseTest {
    private val useCase = CountParcelsWithCarrierUseCase(
        StubParcelStatistics(GARDEN_TOOLS_SELLER to 37, LIGHTING_SELLER to 112),
        ParcelAccessPolicy(),
    )

    @Test
    fun `a seller counts its own parcels with the carrier`() {
        assertThat(useCase.count(aSellerViewer(GARDEN_TOOLS_SELLER), null)).isEqualTo(37)
    }

    @Test
    fun `a support agent counts the parcels of the requested seller`() {
        assertThat(useCase.count(aSupportAgent(), LIGHTING_SELLER)).isEqualTo(112)
    }

    @Test
    fun `a seller cannot count the parcels of another seller`() {
        assertThatThrownBy { useCase.count(aSellerViewer(GARDEN_TOOLS_SELLER), LIGHTING_SELLER) }
            .isInstanceOf(AccessDeniedException::class.java)
    }

    @Test
    fun `a support agent must name the seller`() {
        assertThatIllegalArgumentException().isThrownBy { useCase.count(aSupportAgent(), null) }
    }
}
