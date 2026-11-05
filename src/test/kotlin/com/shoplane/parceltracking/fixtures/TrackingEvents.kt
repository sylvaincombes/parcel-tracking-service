package com.shoplane.parceltracking.fixtures

import com.shoplane.parceltracking.domain.parcel.TrackingEvent
import com.shoplane.parceltracking.domain.parcel.TrackingEventType
import java.time.Instant

object TrackingEvents {
    private const val LYON_HUB = "Lyon hub"

    fun pickedUp(at: String) = trackingEvent(TrackingEventType.PICKED_UP, at)

    fun inTransit(at: String) = trackingEvent(TrackingEventType.IN_TRANSIT, at)

    fun arrivedAtHub(at: String, location: String = LYON_HUB) = trackingEvent(TrackingEventType.ARRIVED_AT_HUB, at, location)

    fun outForDelivery(at: String) = trackingEvent(TrackingEventType.OUT_FOR_DELIVERY, at)

    fun delivered(at: String) = trackingEvent(TrackingEventType.DELIVERED, at)

    private fun trackingEvent(type: TrackingEventType, at: String, location: String = LYON_HUB) =
        TrackingEvent(type = type, occurredAt = Instant.parse(at), location = location)
}
