package com.shoplane.parceltracking.infra.carrierregistry

import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.carrier.CarrierStatus
import com.shoplane.parceltracking.domain.carrier.TransitDays
import feign.FeignException
import feign.Request
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

class CarrierRegistryAdapterTest {
    private val client = mockk<CarrierRegistryClient>()
    private val adapter = CarrierRegistryAdapter(client)

    @Test
    fun `maps an active carrier`() {
        every { client.getCarrier("colisway") } returns activeColisway()

        val carrier = adapter.findCarrier(CarrierCode("colisway"))

        assertThat(carrier?.displayName).isEqualTo("Colisway")
        assertThat(carrier?.transitDays).isEqualTo(TransitDays(1, 3))
        assertThat(carrier?.status).isEqualTo(CarrierStatus.ACTIVE)
        assertThat(carrier?.replacedBy).isNull()
    }

    @Test
    fun `a carrier unknown to the registry is absent`() {
        every { client.getCarrier("speedyship") } throws notFound()

        assertThat(adapter.findCarrier(CarrierCode("speedyship"))).isNull()
    }

    private fun activeColisway() =
        CarrierDto(
            code = "colisway",
            displayName = "Colisway",
            trackingUrlTemplate = "https://track.colisway.example/parcels/{trackingNumber}",
            transitDays = TransitDaysDto(1, 3),
            status = CarrierStatusDto.ACTIVE,
        )

    private fun notFound(): FeignException.NotFound {
        val request = Request.create(
            Request.HttpMethod.GET,
            "/v2/carriers/speedyship",
            emptyMap(),
            null,
            StandardCharsets.UTF_8,
            null,
        )
        return FeignException.NotFound("Carrier not found", request, null, emptyMap())
    }
}
