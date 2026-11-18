package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.parcel.PageRequest
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import com.shoplane.parceltracking.domain.parcel.SellerId
import com.shoplane.parceltracking.domain.transit.TransitTimeCalculator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class ParcelDetail(
    val parcel: Parcel,
    val transitBusinessDays: Int?,
)

@Service
@Transactional(readOnly = true)
class BackOfficeParcelQueries(
    private val parcels: ParcelRepository,
    private val accessPolicy: ParcelAccessPolicy,
    private val transitTime: TransitTimeCalculator,
) {
    fun list(actor: BackOfficeActor, requestedSellerId: SellerId?, page: PageRequest): List<Parcel> {
        val sellerId = accessPolicy.resolveSellerScope(actor, requestedSellerId)
        return parcels.findBySeller(sellerId, page)
    }

    fun detail(actor: BackOfficeActor, parcelId: ParcelId): ParcelDetail {
        val parcel = parcels.findById(parcelId) ?: throw ParcelNotFoundException(parcelId.toString())
        accessPolicy.requireCanView(actor, parcel)
        return ParcelDetail(parcel, transitTime.businessDaysInTransit(parcel))
    }
}
