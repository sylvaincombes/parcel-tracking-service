package com.shoplane.parceltracking.presentation.backoffice

import com.shoplane.parceltracking.application.ListActiveCarriersUseCase
import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.domain.carrier.Carrier
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/back-office/v1/carriers")
class BackOfficeCarrierController(
    private val listActiveCarriers: ListActiveCarriersUseCase,
) {
    @GetMapping
    fun list(actor: BackOfficeActor): List<CarrierOptionResponse> =
        listActiveCarriers.list().map(CarrierOptionResponse::from)
}

data class CarrierOptionResponse(
    val code: String,
    val displayName: String,
) {
    companion object {
        fun from(carrier: Carrier) = CarrierOptionResponse(code = carrier.code.value, displayName = carrier.displayName)
    }
}
