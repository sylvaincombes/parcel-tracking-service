package com.shoplane.parceltracking.infra.persistence

import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.parcel.OrderReference
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.SellerId
import com.shoplane.parceltracking.domain.parcel.TrackingEvent
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import java.time.Instant

object ParcelEntityMapper {
    fun toDomain(entity: ParcelEntity): Parcel =
        Parcel(
            id = ParcelId(entity.id),
            trackingNumber = TrackingNumber.of(entity.trackingNumber),
            carrierCode = CarrierCode(entity.carrierCode),
            sellerId = SellerId(entity.sellerId),
            orderReference = OrderReference(entity.orderReference),
            registeredAt = entity.registeredAt,
            status = entity.status,
            deliveryInstructions = entity.deliveryInstructions,
            events = entity.events.map(::toDomainEvent),
            pickedUpOn = entity.pickedUpOn,
            deliveredAt = entity.deliveredAt,
            lateDetectedAt = entity.lateDetectedAt,
            version = entity.version,
        )

    fun toNewEntity(parcel: Parcel, now: Instant): ParcelEntity =
        ParcelEntity(
            id = parcel.id.value,
            trackingNumber = parcel.trackingNumber.value,
            carrierCode = parcel.carrierCode.value,
            sellerId = parcel.sellerId.value,
            orderReference = parcel.orderReference.value,
            status = parcel.status,
            deliveryInstructions = parcel.deliveryInstructions,
            pickedUpOn = parcel.pickedUpOn,
            deliveredAt = parcel.deliveredAt,
            lateDetectedAt = parcel.lateDetectedAt,
            registeredAt = parcel.registeredAt,
            updatedAt = now,
        ).also { appendNewEvents(it, parcel, now) }

    fun applyChanges(entity: ParcelEntity, parcel: Parcel, now: Instant) {
        entity.orderReference = parcel.orderReference.value
        entity.status = parcel.status
        entity.deliveryInstructions = parcel.deliveryInstructions
        entity.pickedUpOn = parcel.pickedUpOn
        entity.deliveredAt = parcel.deliveredAt
        entity.lateDetectedAt = parcel.lateDetectedAt
        entity.updatedAt = now
        appendNewEvents(entity, parcel, now)
    }

    private fun appendNewEvents(entity: ParcelEntity, parcel: Parcel, now: Instant) {
        parcel.events.drop(entity.events.size).forEach { event ->
            entity.events += TrackingEventEntity(
                parcel = entity,
                type = event.type,
                occurredAt = event.occurredAt,
                location = event.location,
                attemptReason = event.attemptReason,
                receivedAt = now,
            )
        }
    }

    private fun toDomainEvent(entity: TrackingEventEntity): TrackingEvent =
        TrackingEvent(
            type = entity.type,
            occurredAt = entity.occurredAt,
            location = entity.location,
            attemptReason = entity.attemptReason,
        )
}
