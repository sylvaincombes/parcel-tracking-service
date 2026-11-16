package com.shoplane.parceltracking.presentation.publicapi

import com.shoplane.parceltracking.application.PublicTracking
import java.time.Instant
import java.time.LocalDate

data class PublicTrackingResponse(
    val trackingNumber: String,
    val status: String,
    val carrierName: String?,
    val carrierTrackingUrl: String?,
    val estimatedDelivery: EstimatedDeliveryResponse?,
    val deliveredAt: Instant?,
    val events: List<PublicTrackingEventResponse>,
) {
    companion object {
        fun from(tracking: PublicTracking): PublicTrackingResponse {
            val parcel = tracking.parcel
            return PublicTrackingResponse(
                trackingNumber = parcel.trackingNumber.value,
                status = parcel.status.name,
                carrierName = tracking.carrierName,
                carrierTrackingUrl = tracking.carrierTrackingLink?.toString(),
                estimatedDelivery = tracking.estimate?.let { EstimatedDeliveryResponse(it.earliest, it.latest) },
                deliveredAt = parcel.deliveredAt,
                events = parcel.events.reversed().map {
                    PublicTrackingEventResponse(type = it.type.name, occurredAt = it.occurredAt, location = it.location)
                },
            )
        }
    }
}

data class EstimatedDeliveryResponse(val earliest: LocalDate, val latest: LocalDate)

data class PublicTrackingEventResponse(val type: String, val occurredAt: Instant, val location: String?)
