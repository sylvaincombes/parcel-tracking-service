package com.shoplane.parceltracking.infra.persistence

import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelSnapshot
import com.shoplane.parceltracking.domain.parcel.ParcelStatus
import com.shoplane.parceltracking.domain.parcel.SellerId
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import java.util.UUID

data class ParcelSnapshotRow(
    val id: UUID,
    val trackingNumber: String,
    val carrierCode: String,
    val sellerId: Long,
    val status: ParcelStatus,
) {
    fun toSnapshot() =
        ParcelSnapshot(
            id = ParcelId(id),
            trackingNumber = TrackingNumber.of(trackingNumber),
            carrierCode = CarrierCode(carrierCode),
            sellerId = SellerId(sellerId),
            status = status,
        )
}
