package com.shoplane.parceltracking.fixtures

import com.shoplane.parceltracking.domain.carrier.Carrier
import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.carrier.CarrierStatus
import com.shoplane.parceltracking.domain.carrier.TransitDays

val COLISWAY = CarrierCode("colisway")
val PARCELWAVE = CarrierCode("parcelwave")

fun aCarrier(
    code: CarrierCode = COLISWAY,
    displayName: String = "Colisway",
    transitDays: TransitDays = TransitDays(min = 1, max = 2),
    status: CarrierStatus = CarrierStatus.ACTIVE,
) = Carrier(
    code = code,
    displayName = displayName,
    trackingUrlTemplate = "https://track.${code.value}.example/parcels/{trackingNumber}",
    transitDays = transitDays,
    status = status,
)
