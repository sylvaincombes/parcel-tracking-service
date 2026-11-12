package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.domain.attempt.DeliveryAttemptClassifier
import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.event.ParcelEventPublisher
import com.shoplane.parceltracking.domain.event.ParcelStatusChanged
import com.shoplane.parceltracking.domain.parcel.IllegalStatusTransitionException
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import com.shoplane.parceltracking.domain.parcel.TrackingEvent
import com.shoplane.parceltracking.domain.parcel.TrackingEventType
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

data class RecordTrackingEvent(
    val carrierCode: CarrierCode,
    val trackingNumber: TrackingNumber,
    val type: TrackingEventType,
    val occurredAt: Instant,
    val location: String?,
    val carrierMessage: String?,
)

enum class RecordingOutcome { STATUS_CHANGED, RECORDED_WITHOUT_STATUS_CHANGE }

@Service
class RecordTrackingEventUseCase(
    private val parcels: ParcelRepository,
    private val publisher: ParcelEventPublisher,
    private val attemptClassifier: DeliveryAttemptClassifier,
    private val clock: Clock,
) {
    @Transactional
    fun record(command: RecordTrackingEvent): RecordingOutcome {
        val parcel = parcels.findSnapshot(command.trackingNumber)
            ?.takeIf { it.carrierCode == command.carrierCode }
            ?: throw ParcelNotFoundException(command.trackingNumber.value)
        val nextStatus = command.type.resultingStatus
        if (!parcel.status.canMoveTo(nextStatus)) {
            throw IllegalStatusTransitionException(parcel.id, parcel.status, nextStatus)
        }
        parcels.appendEvent(parcel.id, trackingEventOf(command), nextStatus)
        if (nextStatus == parcel.status) {
            return RecordingOutcome.RECORDED_WITHOUT_STATUS_CHANGE
        }
        publisher.publish(
            ParcelStatusChanged(
                parcelId = parcel.id,
                trackingNumber = parcel.trackingNumber,
                sellerId = parcel.sellerId,
                previousStatus = parcel.status,
                currentStatus = nextStatus,
                occurredAt = clock.instant(),
            ),
        )
        return RecordingOutcome.STATUS_CHANGED
    }

    private fun trackingEventOf(command: RecordTrackingEvent) =
        TrackingEvent(
            type = command.type,
            occurredAt = command.occurredAt,
            location = command.location,
            attemptReason = attemptReasonOf(command),
        )

    private fun attemptReasonOf(command: RecordTrackingEvent) =
        if (command.type == TrackingEventType.DELIVERY_ATTEMPT_FAILED) {
            attemptClassifier.classify(command.carrierMessage.orEmpty())
        } else {
            null
        }
}
