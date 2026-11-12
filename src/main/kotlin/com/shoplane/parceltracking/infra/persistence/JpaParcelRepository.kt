package com.shoplane.parceltracking.infra.persistence

import com.shoplane.parceltracking.domain.parcel.PageRequest
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import com.shoplane.parceltracking.domain.parcel.ParcelSnapshot
import com.shoplane.parceltracking.domain.parcel.ParcelStatus
import com.shoplane.parceltracking.domain.parcel.SellerId
import com.shoplane.parceltracking.domain.parcel.TrackingEvent
import com.shoplane.parceltracking.domain.parcel.TrackingEventType
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Repository
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import org.springframework.data.domain.PageRequest as SpringPageRequest

@Repository
class JpaParcelRepository(
    private val jpa: ParcelJpaRepository,
    private val trackingEvents: TrackingEventJpaRepository,
    private val clock: Clock,
) : ParcelRepository {
    override fun findById(id: ParcelId): Parcel? = jpa.findById(id.value).map(ParcelEntityMapper::toDomain).orElse(null)

    override fun findByTrackingNumber(trackingNumber: TrackingNumber): Parcel? =
        jpa.findByTrackingNumber(trackingNumber.value)?.let(ParcelEntityMapper::toDomain)

    override fun findSnapshot(trackingNumber: TrackingNumber): ParcelSnapshot? =
        jpa.findSnapshotRow(trackingNumber.value)?.toSnapshot()

    override fun findBySeller(sellerId: SellerId, page: PageRequest): List<Parcel> =
        jpa.findBySellerIdOrderByRegisteredAtDesc(sellerId.value, SpringPageRequest.of(page.page, page.size))
            .map(ParcelEntityMapper::toDomain)

    override fun findInFlightPickedUp(after: ParcelId?, limit: Int): List<Parcel> =
        jpa.findInFlightPickedUp(IN_FLIGHT_STATUSES, after?.value, SpringPageRequest.ofSize(limit))
            .map(ParcelEntityMapper::toDomain)

    override fun save(parcel: Parcel): Parcel {
        val now = clock.instant()
        val existing = jpa.findById(parcel.id.value).orElse(null)
            ?: return ParcelEntityMapper.toDomain(jpa.saveAndFlush(ParcelEntityMapper.toNewEntity(parcel, now)))
        if (existing.version != parcel.version) {
            throw ObjectOptimisticLockingFailureException(ParcelEntity::class.java, parcel.id.value)
        }
        ParcelEntityMapper.applyChanges(existing, parcel, now)
        return ParcelEntityMapper.toDomain(jpa.saveAndFlush(existing))
    }

    override fun appendEvent(parcelId: ParcelId, event: TrackingEvent, status: ParcelStatus) {
        val now = clock.instant()
        trackingEvents.save(
            TrackingEventEntity(
                parcel = jpa.getReferenceById(parcelId.value),
                type = event.type,
                occurredAt = event.occurredAt,
                location = event.location,
                attemptReason = event.attemptReason,
                receivedAt = now,
            ),
        )
        jpa.updateStatus(
            id = parcelId.value,
            status = status,
            pickedUpOn = pickUpDayOf(event),
            deliveredAt = event.occurredAt.takeIf { status == ParcelStatus.DELIVERED },
            updatedAt = now,
        )
    }

    private fun pickUpDayOf(event: TrackingEvent): LocalDate? =
        if (event.type == TrackingEventType.PICKED_UP) LocalDate.ofInstant(event.occurredAt, ZoneOffset.UTC) else null

    private companion object {
        val IN_FLIGHT_STATUSES = ParcelStatus.entries.filterNot { it.isTerminal || it == ParcelStatus.REGISTERED }
    }
}
