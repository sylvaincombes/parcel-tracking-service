package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.domain.carrier.Carrier
import com.shoplane.parceltracking.domain.carrier.CarrierDirectory
import com.shoplane.parceltracking.domain.eta.DeliveryEstimate
import com.shoplane.parceltracking.domain.eta.EtaCalculator
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI

data class PublicTracking(
    val parcel: Parcel,
    val carrierName: String?,
    val carrierTrackingLink: URI?,
    val estimate: DeliveryEstimate?,
)

@Service
class GetPublicTrackingUseCase(
    private val parcels: ParcelRepository,
    private val carriers: CarrierDirectory,
    private val etaCalculator: EtaCalculator,
) {
    @Transactional(readOnly = true)
    fun track(trackingNumber: TrackingNumber): PublicTracking {
        val parcel = parcels.findByTrackingNumber(trackingNumber)
            ?: throw ParcelNotFoundException(trackingNumber.value)
        val carrier = carriers.findCarrier(parcel.carrierCode)
        return PublicTracking(
            parcel = parcel,
            carrierName = carrier?.displayName,
            carrierTrackingLink = carrier?.trackingLinkFor(parcel.trackingNumber),
            estimate = carrier?.let { estimateFor(parcel, it) },
        )
    }

    private fun estimateFor(parcel: Parcel, carrier: Carrier): DeliveryEstimate? {
        val pickedUpOn = parcel.pickedUpOn ?: return null
        if (parcel.status.isTerminal) {
            return null
        }
        return etaCalculator.estimate(pickedUpOn, carrier.transitDays)
    }
}
