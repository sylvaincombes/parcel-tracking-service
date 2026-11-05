package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.carrier.CarrierStatus
import com.shoplane.parceltracking.domain.carrier.RetiredCarrierException
import com.shoplane.parceltracking.domain.carrier.UnknownCarrierException
import com.shoplane.parceltracking.domain.parcel.DuplicateTrackingNumberException
import com.shoplane.parceltracking.domain.parcel.OrderReference
import com.shoplane.parceltracking.domain.parcel.ParcelDimensions
import com.shoplane.parceltracking.domain.parcel.ParcelStatus
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import com.shoplane.parceltracking.fixtures.COLISWAY
import com.shoplane.parceltracking.fixtures.GARDEN_TOOLS_SELLER
import com.shoplane.parceltracking.fixtures.InMemoryParcelRepository
import com.shoplane.parceltracking.fixtures.PARCELWAVE
import com.shoplane.parceltracking.fixtures.StubCarrierDirectory
import com.shoplane.parceltracking.fixtures.aCarrier
import com.shoplane.parceltracking.fixtures.aRegisteredParcel
import com.shoplane.parceltracking.fixtures.aSellerAdmin
import com.shoplane.parceltracking.fixtures.aSupportAgent
import com.shoplane.parceltracking.fixtures.fixedClockAt
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class RegisterParcelUseCaseTest {
    private val parcels = InMemoryParcelRepository()
    private val carriers = StubCarrierDirectory(
        aCarrier(code = COLISWAY),
        aCarrier(code = PARCELWAVE, status = CarrierStatus.RETIRED, replacedBy = COLISWAY),
    )
    private val useCase = RegisterParcelUseCase(parcels, carriers, fixedClockAt("2026-11-02T08:30:00Z"))

    @Test
    fun `registers a parcel for the seller of the back-office user`() {
        val parcel = useCase.register(aSellerAdmin(), command())

        assertThat(parcel.sellerId).isEqualTo(GARDEN_TOOLS_SELLER)
        assertThat(parcel.status).isEqualTo(ParcelStatus.REGISTERED)
        assertThat(parcels.saved).hasSize(1)
    }

    @Test
    fun `keeps the dimensions declared by the seller`() {
        val dimensions = ParcelDimensions(BigDecimal("2.350"), BigDecimal("40.0"), BigDecimal("30.0"), BigDecimal("12.5"))

        val parcel = useCase.register(aSellerAdmin(), command().copy(dimensions = dimensions))

        assertThat(parcel.dimensions).isEqualTo(dimensions)
    }

    @Test
    fun `refuses a carrier unknown to the registry`() {
        assertThatThrownBy { useCase.register(aSellerAdmin(), command(carrierCode = CarrierCode("speedyship"))) }
            .isInstanceOf(UnknownCarrierException::class.java)
    }

    @Test
    fun `refuses a retired carrier`() {
        assertThatThrownBy { useCase.register(aSellerAdmin(), command(carrierCode = PARCELWAVE)) }
            .isInstanceOf(RetiredCarrierException::class.java)
    }

    @Test
    fun `refuses a tracking number already registered`() {
        parcels.save(aRegisteredParcel(trackingNumber = "CW4400112233FR"))

        assertThatThrownBy { useCase.register(aSellerAdmin(), command(trackingNumber = "CW4400112233FR")) }
            .isInstanceOf(DuplicateTrackingNumberException::class.java)
    }

    @Test
    fun `support agents cannot register parcels on behalf of a seller`() {
        assertThatThrownBy { useCase.register(aSupportAgent(), command()) }
            .isInstanceOf(AccessDeniedException::class.java)
    }

    private fun command(trackingNumber: String = "CW4400112233FR", carrierCode: CarrierCode = COLISWAY) =
        RegisterParcel(
            trackingNumber = TrackingNumber.of(trackingNumber),
            carrierCode = carrierCode,
            orderReference = OrderReference("SO-2026-118734"),
            deliveryInstructions = "Leave at the garden gate",
        )
}
