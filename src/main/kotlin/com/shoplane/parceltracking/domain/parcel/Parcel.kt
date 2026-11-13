package com.shoplane.parceltracking.domain.parcel

import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.event.ParcelStatusChanged
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class Parcel(
    val id: ParcelId,
    val trackingNumber: TrackingNumber,
    val carrierCode: CarrierCode,
    val sellerId: SellerId,
    val orderReference: OrderReference,
    val registeredAt: Instant,
    status: ParcelStatus = ParcelStatus.REGISTERED,
    deliveryInstructions: String? = null,
    events: List<TrackingEvent> = emptyList(),
    pickedUpOn: LocalDate? = null,
    deliveredAt: Instant? = null,
    lateDetectedAt: Instant? = null,
    val version: Long? = null,
) {
    var status: ParcelStatus = status
        private set

    var deliveryInstructions: String? = deliveryInstructions
        private set

    var pickedUpOn: LocalDate? = pickedUpOn
        private set

    var deliveredAt: Instant? = deliveredAt
        private set

    var lateDetectedAt: Instant? = lateDetectedAt
        private set

    private val recordedEvents: MutableList<TrackingEvent> = events.toMutableList()

    val events: List<TrackingEvent>
        get() = recordedEvents.toList()

    val isLate: Boolean
        get() = lateDetectedAt != null

    fun timeline(): List<TrackingEvent> = recordedEvents.sortedBy { it.occurredAt }

    fun hasRecorded(carrierEventId: String): Boolean = recordedEvents.any { it.carrierEventId == carrierEventId }

    fun record(event: TrackingEvent, now: Instant): ParcelStatusChanged? {
        val next = event.type.resultingStatus
        if (!status.canMoveTo(next)) {
            throw IllegalStatusTransitionException(id, status, next)
        }
        recordedEvents += event
        if (event.type == TrackingEventType.PICKED_UP && pickedUpOn == null) {
            pickedUpOn = LocalDate.ofInstant(event.occurredAt, ZoneOffset.UTC)
        }
        if (next == ParcelStatus.DELIVERED) {
            deliveredAt = event.occurredAt
        }
        return moveTo(next, now)
    }

    fun cancel(now: Instant): ParcelStatusChanged {
        if (!status.canMoveTo(ParcelStatus.CANCELLED)) {
            throw IllegalStatusTransitionException(id, status, ParcelStatus.CANCELLED)
        }
        return requireNotNull(moveTo(ParcelStatus.CANCELLED, now))
    }

    fun markLate(now: Instant) {
        check(!status.isTerminal) { "Parcel $id is already $status" }
        if (lateDetectedAt == null) {
            lateDetectedAt = now
        }
    }

    private fun moveTo(next: ParcelStatus, now: Instant): ParcelStatusChanged? {
        if (next == status) {
            return null
        }
        val previous = status
        status = next
        return ParcelStatusChanged(
            parcelId = id,
            trackingNumber = trackingNumber,
            sellerId = sellerId,
            previousStatus = previous,
            currentStatus = next,
            occurredAt = now,
        )
    }

    companion object {
        fun register(
            trackingNumber: TrackingNumber,
            carrierCode: CarrierCode,
            sellerId: SellerId,
            orderReference: OrderReference,
            deliveryInstructions: String?,
            now: Instant,
        ): Parcel =
            Parcel(
                id = ParcelId.generate(),
                trackingNumber = trackingNumber,
                carrierCode = carrierCode,
                sellerId = sellerId,
                orderReference = orderReference,
                registeredAt = now,
                deliveryInstructions = deliveryInstructions?.trim()?.takeIf { it.isNotEmpty() },
            )
    }
}
