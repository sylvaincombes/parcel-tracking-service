package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.domain.carrier.Carrier
import com.shoplane.parceltracking.domain.carrier.CarrierDirectory
import org.springframework.stereotype.Service

@Service
class ListActiveCarriersUseCase(
    private val carriers: CarrierDirectory,
) {
    fun list(): List<Carrier> = carriers.activeCarriers()
}
