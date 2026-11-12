package com.shoplane.parceltracking.domain.parcel

import java.time.Instant

interface ParcelRepository {
    fun findById(id: ParcelId): Parcel?

    fun findByTrackingNumber(trackingNumber: TrackingNumber): Parcel?

    fun findBySeller(sellerId: SellerId, page: PageRequest): List<Parcel>

    fun findBySellerRegisteredBetween(sellerId: SellerId, from: Instant, until: Instant): List<Parcel>

    fun findInFlightPickedUp(after: ParcelId?, limit: Int): List<Parcel>

    fun save(parcel: Parcel): Parcel
}

data class PageRequest(val page: Int, val size: Int) {
    init {
        require(page >= 0) { "Page must not be negative" }
        require(size in 1..MAX_SIZE) { "Page size must be between 1 and $MAX_SIZE" }
    }

    companion object {
        const val MAX_SIZE = 200
    }
}
