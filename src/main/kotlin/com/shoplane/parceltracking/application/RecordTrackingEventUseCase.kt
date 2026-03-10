package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.domain.attempt.DeliveryAttemptClassifier
import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.event.ParcelEventPublisher
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
        val parcel = parcels.findByTrackingNumber(command.trackingNumber)
            ?.takeIf { it.carrierCode == command.carrierCode }
            ?: throw ParcelNotFoundException(command.trackingNumber.value)
        val event = TrackingEvent(
            type = command.type,
            occurredAt = command.occurredAt,
            location = command.location,
            attemptReason = attemptReasonOf(command),
        )
        val statusChange = parcel.record(event, clock.instant())
        parcels.save(parcel)
        statusChange?.let(publisher::publish)
        return if (statusChange != null) RecordingOutcome.STATUS_CHANGED else RecordingOutcome.RECORDED_WITHOUT_STATUS_CHANGE
    }

    private fun attemptReasonOf(command: RecordTrackingEvent) =
        if (command.type == TrackingEventType.DELIVERY_ATTEMPT_FAILED) {
            attemptClassifier.classify(command.carrierMessage.orEmpty())
        } else {
            null
        }
}
