package com.shoplane.parceltracking.presentation.webhook

import com.shoplane.parceltracking.domain.parcel.TrackingEventType

object CarrierEventCodeMapper {
    private val typesByCode: Map<String, TrackingEventType> =
        mapOf(
            "PU" to TrackingEventType.PICKED_UP,
            "TR" to TrackingEventType.IN_TRANSIT,
            "HUB" to TrackingEventType.ARRIVED_AT_HUB,
            "OFD" to TrackingEventType.OUT_FOR_DELIVERY,
            "ATT" to TrackingEventType.DELIVERY_ATTEMPT_FAILED,
            "PPT" to TrackingEventType.ARRIVED_AT_PICKUP_POINT,
            "DLV" to TrackingEventType.DELIVERED,
            "RTS" to TrackingEventType.RETURNED_TO_SENDER,
        )

    fun typeOf(carrierEventCode: String): TrackingEventType? = typesByCode[carrierEventCode.trim().uppercase()]
}
