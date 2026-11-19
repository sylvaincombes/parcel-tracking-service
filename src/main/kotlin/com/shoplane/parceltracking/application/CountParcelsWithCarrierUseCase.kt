package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.parcel.ParcelStatistics
import com.shoplane.parceltracking.domain.parcel.SellerId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CountParcelsWithCarrierUseCase(
    private val statistics: ParcelStatistics,
    private val accessPolicy: ParcelAccessPolicy,
) {
    @Transactional(readOnly = true)
    fun count(actor: BackOfficeActor, requestedSellerId: SellerId?): Long {
        val sellerId = accessPolicy.resolveSellerScope(actor, requestedSellerId)
        return statistics.countWithCarrier(sellerId)
    }
}
