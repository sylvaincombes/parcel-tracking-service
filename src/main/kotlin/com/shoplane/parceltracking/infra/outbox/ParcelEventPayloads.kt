package com.shoplane.parceltracking.infra.outbox

import com.shoplane.parceltracking.domain.event.ParcelDomainEvent
import com.shoplane.parceltracking.domain.event.ParcelLateDetected
import com.shoplane.parceltracking.domain.event.ParcelStatusChanged
import java.time.Instant
import java.time.LocalDate

data class ParcelStatusChangedPayload(
    val parcelId: String,
    val trackingNumber: String,
    val carrier: String,
    val sellerId: Long,
    val previousStatus: String,
    val currentStatus: String,
    val occurredAt: Instant,
)

data class ParcelLateDetectedPayload(
    val parcelId: String,
    val trackingNumber: String,
    val sellerId: Long,
    val deliveryDeadline: LocalDate,
    val occurredAt: Instant,
)

object ParcelEventPayloads {
    fun eventTypeOf(event: ParcelDomainEvent): String =
        when (event) {
            is ParcelStatusChanged -> "parcel.status-changed"
            is ParcelLateDetected -> "parcel.late-detected"
        }

    fun payloadOf(event: ParcelDomainEvent): Any =
        when (event) {
            is ParcelStatusChanged ->
                ParcelStatusChangedPayload(
                    parcelId = event.parcelId.toString(),
                    trackingNumber = event.trackingNumber.value,
                    carrier = event.carrierCode.value,
                    sellerId = event.sellerId.value,
                    previousStatus = event.previousStatus.name,
                    currentStatus = event.currentStatus.name,
                    occurredAt = event.occurredAt,
                )
            is ParcelLateDetected ->
                ParcelLateDetectedPayload(
                    parcelId = event.parcelId.toString(),
                    trackingNumber = event.trackingNumber.value,
                    sellerId = event.sellerId.value,
                    deliveryDeadline = event.deliveryDeadline,
                    occurredAt = event.occurredAt,
                )
        }
}
