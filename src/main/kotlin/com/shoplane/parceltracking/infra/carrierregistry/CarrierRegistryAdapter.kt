package com.shoplane.parceltracking.infra.carrierregistry

import com.shoplane.parceltracking.domain.carrier.Carrier
import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.carrier.CarrierDirectory
import com.shoplane.parceltracking.domain.carrier.CarrierStatus
import com.shoplane.parceltracking.domain.carrier.TransitDays
import feign.FeignException
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class CarrierRegistryAdapter(
    private val client: CarrierRegistryClient,
) : CarrierDirectory {
    @Cacheable(cacheNames = [CARRIERS_CACHE], unless = "#result == null")
    override fun findCarrier(code: CarrierCode): Carrier? =
        try {
            toDomain(client.getCarrier(code.value))
        } catch (_: FeignException.NotFound) {
            null
        }

    companion object {
        const val CARRIERS_CACHE = "carriers"

        fun toDomain(dto: CarrierDto): Carrier =
            Carrier(
                code = CarrierCode(dto.code),
                displayName = dto.displayName,
                trackingUrlTemplate = dto.trackingUrlTemplate,
                transitDays = TransitDays(min = dto.transitDays.min, max = dto.transitDays.max),
                status = when (dto.status) {
                    CarrierStatusDto.ACTIVE -> CarrierStatus.ACTIVE
                    CarrierStatusDto.RETIRED -> CarrierStatus.RETIRED
                },
                replacedBy = dto.replacedBy?.let(::CarrierCode),
            )
    }
}
