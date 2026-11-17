package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import org.springframework.stereotype.Service

@Service
class FindParcelByTrackingNumberUseCase(
    private val parcels: ParcelRepository,
    private val accessPolicy: ParcelAccessPolicy,
) {
    fun find(actor: BackOfficeActor, trackingNumber: TrackingNumber): Parcel {
        val parcel = parcels.findByTrackingNumber(trackingNumber) ?: throw ParcelNotFoundException(trackingNumber.value)
        accessPolicy.requireCanView(actor, parcel)
        return parcel
    }
}
