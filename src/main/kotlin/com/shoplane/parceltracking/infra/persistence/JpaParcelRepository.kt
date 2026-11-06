package com.shoplane.parceltracking.infra.persistence

import com.shoplane.parceltracking.domain.parcel.PageRequest
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import com.shoplane.parceltracking.domain.parcel.ParcelStatus
import com.shoplane.parceltracking.domain.parcel.SellerId
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Repository
import java.time.Clock
import org.springframework.data.domain.PageRequest as SpringPageRequest

@Repository
class JpaParcelRepository(
    private val jpa: ParcelJpaRepository,
    private val clock: Clock,
) : ParcelRepository {
    override fun findById(id: ParcelId): Parcel? = jpa.findById(id.value).map(ParcelEntityMapper::toDomain).orElse(null)

    override fun findByTrackingNumber(trackingNumber: TrackingNumber): Parcel? =
        jpa.findByTrackingNumber(trackingNumber.value)?.let(ParcelEntityMapper::toDomain)

    override fun findBySeller(sellerId: SellerId, page: PageRequest): List<Parcel> =
        jpa.findBySellerIdOrderByRegisteredAtDesc(sellerId.value, SpringPageRequest.of(page.page, page.size))
            .map(ParcelEntityMapper::toDomain)

    override fun findLateBySeller(sellerId: SellerId, page: PageRequest): List<Parcel> =
        jpa.findBySellerIdAndLateDetectedAtIsNotNullOrderByLateDetectedAtDesc(
            sellerId.value,
            SpringPageRequest.of(page.page, page.size),
        ).map(ParcelEntityMapper::toDomain)

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

    private companion object {
        val IN_FLIGHT_STATUSES = ParcelStatus.entries.filterNot { it.isTerminal || it == ParcelStatus.REGISTERED }
    }
}
