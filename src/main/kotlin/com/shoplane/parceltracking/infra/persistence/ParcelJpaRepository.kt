package com.shoplane.parceltracking.infra.persistence

import com.shoplane.parceltracking.domain.parcel.ParcelStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

interface ParcelJpaRepository : JpaRepository<ParcelEntity, UUID> {
    fun findByTrackingNumber(trackingNumber: String): ParcelEntity?

    @Query(
        """
        select new com.shoplane.parceltracking.infra.persistence.ParcelSnapshotRow(
            p.id, p.trackingNumber, p.carrierCode, p.sellerId, p.status
        )
        from ParcelEntity p
        where p.trackingNumber = :trackingNumber
        """,
    )
    fun findSnapshotRow(@Param("trackingNumber") trackingNumber: String): ParcelSnapshotRow?

    @Modifying
    @Query(
        """
        update ParcelEntity p
        set p.status = :status,
            p.pickedUpOn = coalesce(p.pickedUpOn, :pickedUpOn),
            p.deliveredAt = coalesce(:deliveredAt, p.deliveredAt),
            p.updatedAt = :updatedAt
        where p.id = :id
        """,
    )
    fun updateStatus(
        @Param("id") id: UUID,
        @Param("status") status: ParcelStatus,
        @Param("pickedUpOn") pickedUpOn: LocalDate?,
        @Param("deliveredAt") deliveredAt: Instant?,
        @Param("updatedAt") updatedAt: Instant,
    ): Int

    fun findBySellerIdOrderByRegisteredAtDesc(sellerId: Long, pageable: Pageable): List<ParcelEntity>

    @Query(
        """
        select p from ParcelEntity p
        where p.status in :statuses
          and p.pickedUpOn is not null
          and p.lateDetectedAt is null
          and (:after is null or p.id > :after)
        order by p.id
        """,
    )
    fun findInFlightPickedUp(
        @Param("statuses") statuses: Collection<ParcelStatus>,
        @Param("after") after: UUID?,
        pageable: Pageable,
    ): List<ParcelEntity>
}
