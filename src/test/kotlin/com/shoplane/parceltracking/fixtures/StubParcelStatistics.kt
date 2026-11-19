package com.shoplane.parceltracking.fixtures

import com.shoplane.parceltracking.domain.parcel.ParcelStatistics
import com.shoplane.parceltracking.domain.parcel.SellerId

class StubParcelStatistics(vararg withCarrierBySeller: Pair<SellerId, Long>) : ParcelStatistics {
    private val withCarrier = withCarrierBySeller.toMap()

    override fun countWithCarrier(sellerId: SellerId): Long = withCarrier[sellerId] ?: 0
}
