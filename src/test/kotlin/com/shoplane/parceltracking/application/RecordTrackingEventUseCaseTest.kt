package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.domain.attempt.DeliveryAttemptClassifier
import com.shoplane.parceltracking.domain.attempt.DeliveryAttemptReason
import com.shoplane.parceltracking.domain.event.ParcelStatusChanged
import com.shoplane.parceltracking.domain.parcel.IllegalStatusTransitionException
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import com.shoplane.parceltracking.domain.parcel.ParcelStatus
import com.shoplane.parceltracking.domain.parcel.TrackingEventType
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import com.shoplane.parceltracking.fixtures.COLISWAY
import com.shoplane.parceltracking.fixtures.InMemoryParcelRepository
import com.shoplane.parceltracking.fixtures.PARCELWAVE
import com.shoplane.parceltracking.fixtures.RecordingParcelEventPublisher
import com.shoplane.parceltracking.fixtures.aPickedUpParcel
import com.shoplane.parceltracking.fixtures.fixedClockAt
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.orm.ObjectOptimisticLockingFailureException
import java.time.Instant

class RecordTrackingEventUseCaseTest {
    private val parcel = aPickedUpParcel(trackingNumber = "CW4400112233FR")
    private val parcels = InMemoryParcelRepository(parcel)
    private val publisher = RecordingParcelEventPublisher()
    private val useCase = RecordTrackingEventUseCase(
        parcels,
        publisher,
        DeliveryAttemptClassifier(),
        fixedClockAt("2026-11-04T12:00:00Z"),
    )

    @Test
    fun `records the event and publishes the status change`() {
        val outcome = useCase.record(command(TrackingEventType.OUT_FOR_DELIVERY))

        assertThat(outcome).isEqualTo(RecordingOutcome.STATUS_CHANGED)
        assertThat(parcels.findById(parcel.id)?.status).isEqualTo(ParcelStatus.OUT_FOR_DELIVERY)
        assertThat(publisher.published.single()).isInstanceOfSatisfying(ParcelStatusChanged::class.java) {
            assertThat(it.currentStatus).isEqualTo(ParcelStatus.OUT_FOR_DELIVERY)
        }
    }

    @Test
    fun `a scan that keeps the status publishes nothing`() {
        useCase.record(command(TrackingEventType.IN_TRANSIT))
        publisher.published.clear()

        val outcome = useCase.record(command(TrackingEventType.ARRIVED_AT_HUB))

        assertThat(outcome).isEqualTo(RecordingOutcome.RECORDED_WITHOUT_STATUS_CHANGE)
        assertThat(publisher.published).isEmpty()
        assertThat(parcels.findById(parcel.id)?.events).hasSize(3)
    }

    @Test
    fun `classifies the reason of a failed delivery attempt`() {
        useCase.record(command(TrackingEventType.OUT_FOR_DELIVERY))

        useCase.record(command(TrackingEventType.DELIVERY_ATTEMPT_FAILED, message = "Recipient absent, notice left"))

        assertThat(parcels.findById(parcel.id)?.events?.last()?.attemptReason)
            .isEqualTo(DeliveryAttemptReason.RECIPIENT_ABSENT)
    }

    @Test
    fun `ignores events sent by another carrier for the same tracking number`() {
        assertThatThrownBy { useCase.record(command(TrackingEventType.OUT_FOR_DELIVERY).copy(carrierCode = PARCELWAVE)) }
            .isInstanceOf(ParcelNotFoundException::class.java)
    }

    @Test
    fun `rejects an event that contradicts the parcel lifecycle`() {
        useCase.record(command(TrackingEventType.OUT_FOR_DELIVERY))
        useCase.record(command(TrackingEventType.DELIVERED))

        assertThatThrownBy { useCase.record(command(TrackingEventType.OUT_FOR_DELIVERY)) }
            .isInstanceOf(IllegalStatusTransitionException::class.java)
    }

    @Test
    fun `retries after a concurrent update and publishes the status change once`() {
        val useCase = useCaseOn(ConcurrentlyUpdatedParcels(parcels, conflictingSaves = 1))

        val outcome = useCase.record(command(TrackingEventType.OUT_FOR_DELIVERY))

        assertThat(outcome).isEqualTo(RecordingOutcome.STATUS_CHANGED)
        assertThat(parcels.findById(parcel.id)?.status).isEqualTo(ParcelStatus.OUT_FOR_DELIVERY)
        assertThat(parcels.findById(parcel.id)?.events).hasSize(2)
        assertThat(publisher.published).hasSize(1)
    }

    @Test
    fun `gives up after three concurrent updates`() {
        val useCase = useCaseOn(ConcurrentlyUpdatedParcels(parcels, conflictingSaves = 3))

        assertThatThrownBy { useCase.record(command(TrackingEventType.OUT_FOR_DELIVERY)) }
            .isInstanceOf(OptimisticLockingFailureException::class.java)
        assertThat(parcels.findById(parcel.id)?.status).isEqualTo(ParcelStatus.PICKED_UP)
        assertThat(publisher.published).isEmpty()
    }

    private fun useCaseOn(repository: ParcelRepository) =
        RecordTrackingEventUseCase(repository, publisher, DeliveryAttemptClassifier(), fixedClockAt("2026-11-04T12:00:00Z"))

    private fun command(type: TrackingEventType, message: String? = null) =
        RecordTrackingEvent(
            carrierCode = COLISWAY,
            trackingNumber = TrackingNumber.of("CW4400112233FR"),
            type = type,
            occurredAt = Instant.parse("2026-11-04T09:15:00Z"),
            location = "Villeurbanne",
            carrierMessage = message,
        )
}

private class ConcurrentlyUpdatedParcels(
    private val delegate: InMemoryParcelRepository,
    private var conflictingSaves: Int,
) : ParcelRepository by delegate {
    override fun save(parcel: Parcel): Parcel {
        if (conflictingSaves > 0) {
            conflictingSaves--
            throw ObjectOptimisticLockingFailureException(Parcel::class.java, parcel.id.value)
        }
        return delegate.save(parcel)
    }
}
