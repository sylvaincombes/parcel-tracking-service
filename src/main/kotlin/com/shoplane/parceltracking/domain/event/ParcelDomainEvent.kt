package com.shoplane.parceltracking.domain.event

import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelStatus
import com.shoplane.parceltracking.domain.parcel.SellerId
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import java.time.Instant
import java.time.LocalDate

sealed interface ParcelDomainEvent {
    val parcelId: ParcelId
    val sellerId: SellerId
    val occurredAt: Instant
}

data class ParcelStatusChanged(
    override val parcelId: ParcelId,
    val trackingNumber: TrackingNumber,
    override val sellerId: SellerId,
    val previousStatus: ParcelStatus,
    val currentStatus: ParcelStatus,
    override val occurredAt: Instant,
) : ParcelDomainEvent

data class ParcelLateDetected(
    override val parcelId: ParcelId,
    val trackingNumber: TrackingNumber,
    override val sellerId: SellerId,
    val deliveryDeadline: LocalDate,
    override val occurredAt: Instant,
) : ParcelDomainEvent

interface ParcelEventPublisher {
    fun publish(event: ParcelDomainEvent)
}
