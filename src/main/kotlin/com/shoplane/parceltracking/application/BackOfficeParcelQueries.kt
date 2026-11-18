package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.carrier.CarrierDirectory
import com.shoplane.parceltracking.domain.parcel.PageRequest
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import com.shoplane.parceltracking.domain.parcel.SellerId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class ParcelListItem(
    val parcel: Parcel,
    val carrierName: String?,
)

@Service
@Transactional(readOnly = true)
class BackOfficeParcelQueries(
    private val parcels: ParcelRepository,
    private val accessPolicy: ParcelAccessPolicy,
    private val carriers: CarrierDirectory,
) {
    fun list(actor: BackOfficeActor, requestedSellerId: SellerId?, page: PageRequest): List<ParcelListItem> {
        val sellerId = accessPolicy.resolveSellerScope(actor, requestedSellerId)
        val sellerParcels = parcels.findBySeller(sellerId, page)
        val carrierNames = sellerParcels
            .map { it.carrierCode }
            .distinct()
            .associateWith { carriers.findCarrier(it)?.displayName }
        return sellerParcels.map { ParcelListItem(it, carrierNames[it.carrierCode]) }
    }

    fun detail(actor: BackOfficeActor, parcelId: ParcelId): Parcel {
        val parcel = parcels.findById(parcelId) ?: throw ParcelNotFoundException(parcelId.toString())
        accessPolicy.requireCanView(actor, parcel)
        return parcel
    }
}
