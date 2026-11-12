package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.fixtures.GARDEN_TOOLS_SELLER
import com.shoplane.parceltracking.fixtures.InMemoryParcelRepository
import com.shoplane.parceltracking.fixtures.LIGHTING_SELLER
import com.shoplane.parceltracking.fixtures.aRegisteredParcel
import com.shoplane.parceltracking.fixtures.aSellerAdmin
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ExportParcelsUseCaseTest {
    private val beforePeriod = aRegisteredParcel(trackingNumber = "CW4400000001FR", registeredAt = "2026-10-31T22:59:00Z")
    private val firstDay = aRegisteredParcel(trackingNumber = "CW4400000002FR", registeredAt = "2026-10-31T23:00:00Z")
    private val lastDay = aRegisteredParcel(trackingNumber = "CW4400000003FR", registeredAt = "2026-11-30T22:59:00Z")
    private val afterPeriod = aRegisteredParcel(trackingNumber = "CW4400000004FR", registeredAt = "2026-11-30T23:00:00Z")
    private val otherSeller = aRegisteredParcel(
        trackingNumber = "CW4400000005FR",
        sellerId = LIGHTING_SELLER,
        registeredAt = "2026-11-10T09:00:00Z",
    )
    private val parcels = InMemoryParcelRepository(lastDay, beforePeriod, otherSeller, firstDay, afterPeriod)
    private val useCase = ExportParcelsUseCase(parcels)

    @Test
    fun `exports the seller's parcels registered during the period, oldest first`() {
        val exported = useCase.export(aSellerAdmin(), GARDEN_TOOLS_SELLER, november())

        assertThat(exported.map { it.id }).containsExactly(firstDay.id, lastDay.id)
    }

    @Test
    fun `period days follow the seller's time zone`() {
        val firstOfNovember = ExportPeriod(LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-01"))

        val exported = useCase.export(aSellerAdmin(), GARDEN_TOOLS_SELLER, firstOfNovember)

        assertThat(exported.map { it.id }).containsExactly(firstDay.id)
    }

    @Test
    fun `a period ending before it starts is rejected`() {
        assertThatIllegalArgumentException().isThrownBy {
            ExportPeriod(LocalDate.parse("2026-11-30"), LocalDate.parse("2026-11-01"))
        }
    }

    @Test
    fun `an export covers at most 92 days`() {
        assertThat(ExportPeriod(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-12-01")).to)
            .isEqualTo(LocalDate.parse("2026-12-01"))
        assertThatIllegalArgumentException().isThrownBy {
            ExportPeriod(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-12-02"))
        }
    }

    private fun november() = ExportPeriod(LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-30"))
}
