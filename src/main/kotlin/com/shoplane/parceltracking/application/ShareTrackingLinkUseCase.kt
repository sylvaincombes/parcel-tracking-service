package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import com.shoplane.parceltracking.domain.parcel.PublicTrackingPage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI

@Service
class ShareTrackingLinkUseCase(
    private val parcels: ParcelRepository,
    private val accessPolicy: ParcelAccessPolicy,
    private val trackingPage: PublicTrackingPage,
) {
    @Transactional(readOnly = true)
    fun linkFor(actor: BackOfficeActor, parcelId: ParcelId): URI {
        val parcel = parcels.findById(parcelId) ?: throw ParcelNotFoundException(parcelId.toString())
        accessPolicy.requireCanView(actor, parcel)
        return trackingPage.linkFor(parcel.trackingNumber)
    }
}
