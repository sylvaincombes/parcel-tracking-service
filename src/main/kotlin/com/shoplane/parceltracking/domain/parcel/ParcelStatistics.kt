package com.shoplane.parceltracking.domain.parcel

interface ParcelStatistics {
    fun countWithCarrier(sellerId: SellerId): Long
}
