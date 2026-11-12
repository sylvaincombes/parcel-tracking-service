package com.shoplane.parceltracking.domain.parcel

import com.shoplane.parceltracking.domain.attempt.DeliveryAttemptReason
import java.time.Instant

enum class TrackingEventType(val resultingStatus: ParcelStatus) {
    PICKED_UP(ParcelStatus.PICKED_UP),
    IN_TRANSIT(ParcelStatus.IN_TRANSIT),
    ARRIVED_AT_HUB(ParcelStatus.IN_TRANSIT),
    OUT_FOR_DELIVERY(ParcelStatus.OUT_FOR_DELIVERY),
    DELIVERY_ATTEMPT_FAILED(ParcelStatus.DELIVERY_FAILED),
    ARRIVED_AT_PICKUP_POINT(ParcelStatus.AWAITING_COLLECTION),
    DELIVERED(ParcelStatus.DELIVERED),
    RETURNED_TO_SENDER(ParcelStatus.RETURNED_TO_SENDER),
}

data class TrackingEvent(
    val type: TrackingEventType,
    val occurredAt: Instant,
    val location: String?,
    val attemptReason: DeliveryAttemptReason? = null,
)
