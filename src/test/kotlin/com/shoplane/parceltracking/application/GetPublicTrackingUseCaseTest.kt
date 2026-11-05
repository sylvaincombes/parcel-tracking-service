package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.domain.calendar.BusinessCalendar
import com.shoplane.parceltracking.domain.carrier.TransitDays
import com.shoplane.parceltracking.domain.eta.EtaCalculator
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import com.shoplane.parceltracking.fixtures.InMemoryParcelRepository
import com.shoplane.parceltracking.fixtures.PARCELWAVE
import com.shoplane.parceltracking.fixtures.StubCarrierDirectory
import com.shoplane.parceltracking.fixtures.TrackingEvents
import com.shoplane.parceltracking.fixtures.aCarrier
import com.shoplane.parceltracking.fixtures.aPickedUpParcel
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Instant

class GetPublicTrackingUseCaseTest {
    private val carriers = StubCarrierDirectory(aCarrier(displayName = "Colisway", transitDays = TransitDays(1, 2)))
    private val etaCalculator = EtaCalculator(BusinessCalendar(emptySet()))

    @Test
    fun `an in-flight parcel shows its carrier link and delivery window`() {
        val parcel = aPickedUpParcel(trackingNumber = "CW4400112233FR", pickedUpAt = "2026-11-16T14:00:00Z")
        val useCase = GetPublicTrackingUseCase(InMemoryParcelRepository(parcel), carriers, etaCalculator)

        val tracking = useCase.track(TrackingNumber.of("CW4400112233FR"))

        assertThat(tracking.carrierName).isEqualTo("Colisway")
        assertThat(tracking.carrierTrackingLink).isEqualTo(URI.create("https://track.colisway.example/parcels/CW4400112233FR"))
        assertThat(tracking.estimate).isNotNull
    }

    @Test
    fun `a delivered parcel has no delivery window`() {
        val parcel = aPickedUpParcel(trackingNumber = "CW4400112233FR").apply {
            record(TrackingEvents.outForDelivery("2026-11-04T06:10:00Z"), Instant.parse("2026-11-04T06:10:00Z"))
            record(TrackingEvents.delivered("2026-11-04T11:42:00Z"), Instant.parse("2026-11-04T11:42:00Z"))
        }
        val useCase = GetPublicTrackingUseCase(InMemoryParcelRepository(parcel), carriers, etaCalculator)

        assertThat(useCase.track(TrackingNumber.of("CW4400112233FR")).estimate).isNull()
    }

    @Test
    fun `a parcel of a carrier missing from the registry is still tracked`() {
        val parcel = aPickedUpParcel(trackingNumber = "PW7781200456FR", carrierCode = PARCELWAVE)
        val useCase = GetPublicTrackingUseCase(InMemoryParcelRepository(parcel), carriers, etaCalculator)

        val tracking = useCase.track(TrackingNumber.of("PW7781200456FR"))

        assertThat(tracking.carrierName).isNull()
        assertThat(tracking.carrierTrackingLink).isNull()
        assertThat(tracking.estimate).isNull()
    }

    @Test
    fun `an unknown tracking number is not found`() {
        val useCase = GetPublicTrackingUseCase(InMemoryParcelRepository(), carriers, etaCalculator)

        assertThatThrownBy { useCase.track(TrackingNumber.of("CW0000000000FR")) }
            .isInstanceOf(ParcelNotFoundException::class.java)
    }
}
