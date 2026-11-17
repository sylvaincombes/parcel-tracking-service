package com.shoplane.parceltracking.fixtures

import com.shoplane.parceltracking.domain.carrier.Carrier
import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.carrier.CarrierDirectory

class StubCarrierDirectory(vararg carriers: Carrier) : CarrierDirectory {
    private val carriersByCode = carriers.associateBy { it.code }

    override fun findCarrier(code: CarrierCode): Carrier? = carriersByCode[code]

    override fun activeCarriers(): List<Carrier> = carriersByCode.values.filter { it.isActive }
}
