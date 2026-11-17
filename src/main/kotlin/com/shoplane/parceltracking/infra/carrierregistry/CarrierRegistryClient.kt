package com.shoplane.parceltracking.infra.carrierregistry

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "carrier-registry", url = "\${parcel-tracking.carrier-registry.url}")
interface CarrierRegistryClient {
    @GetMapping("/v2/carriers/{code}")
    fun getCarrier(@PathVariable("code") code: String): CarrierDto

    @GetMapping("/v2/carriers")
    fun listCarriers(@RequestParam("state") status: String?): List<CarrierDto>
}
