package com.shoplane.parceltracking.domain.parcel

import com.shoplane.parceltracking.domain.carrier.CarrierCode

data class ParcelSnapshot(
    val id: ParcelId,
    val trackingNumber: TrackingNumber,
    val carrierCode: CarrierCode,
    val sellerId: SellerId,
    val status: ParcelStatus,
)
