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
import java.net.URI

data class ParcelDetail(
    val parcel: Parcel,
    val carrierTrackingLink: URI?,
)

@Service
@Transactional(readOnly = true)
class BackOfficeParcelQueries(
    private val parcels: ParcelRepository,
    private val accessPolicy: ParcelAccessPolicy,
    private val carriers: CarrierDirectory,
) {
    fun list(actor: BackOfficeActor, requestedSellerId: SellerId?, page: PageRequest): List<Parcel> {
        val sellerId = accessPolicy.resolveSellerScope(actor, requestedSellerId)
        return parcels.findBySeller(sellerId, page)
    }

    fun detail(actor: BackOfficeActor, parcelId: ParcelId): ParcelDetail {
        val parcel = parcels.findById(parcelId) ?: throw ParcelNotFoundException(parcelId.toString())
        accessPolicy.requireCanView(actor, parcel)
        val carrier = carriers.findCarrier(parcel.carrierCode)
        return ParcelDetail(parcel, carrier?.trackingLinkFor(parcel.trackingNumber))
    }
}
