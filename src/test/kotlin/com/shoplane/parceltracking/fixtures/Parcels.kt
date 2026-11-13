package com.shoplane.parceltracking.fixtures

import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.parcel.OrderReference
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.SellerId
import com.shoplane.parceltracking.domain.parcel.TrackingEvent
import com.shoplane.parceltracking.domain.parcel.TrackingEventType
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import java.time.Instant

val GARDEN_TOOLS_SELLER = SellerId(4101)
val LIGHTING_SELLER = SellerId(5230)

fun aRegisteredParcel(
    trackingNumber: String = "CW4400112233FR",
    carrierCode: CarrierCode = COLISWAY,
    sellerId: SellerId = GARDEN_TOOLS_SELLER,
    orderReference: String = "SO-2026-118734",
    registeredAt: String = "2026-11-02T08:30:00Z",
    deliveryInstructions: String? = null,
) = Parcel(
    id = ParcelId.generate(),
    trackingNumber = TrackingNumber.of(trackingNumber),
    carrierCode = carrierCode,
    sellerId = sellerId,
    orderReference = OrderReference(orderReference),
    registeredAt = Instant.parse(registeredAt),
    deliveryInstructions = deliveryInstructions,
)

fun aPickedUpParcel(
    pickedUpAt: String = "2026-11-02T14:00:00Z",
    sellerId: SellerId = GARDEN_TOOLS_SELLER,
    trackingNumber: String = "CW4400112233FR",
    carrierCode: CarrierCode = COLISWAY,
): Parcel =
    aRegisteredParcel(trackingNumber = trackingNumber, sellerId = sellerId, carrierCode = carrierCode).apply {
        record(event(TrackingEventType.PICKED_UP, pickedUpAt), Instant.parse(pickedUpAt))
    }

fun event(
    type: TrackingEventType,
    occurredAt: String,
    location: String? = "Lyon hub",
    carrierEventId: String = "evt-${type.name.lowercase()}-$occurredAt",
) = TrackingEvent(carrierEventId = carrierEventId, type = type, occurredAt = Instant.parse(occurredAt), location = location)
