package com.shoplane.parceltracking.infra.persistence

import com.shoplane.parceltracking.domain.parcel.ParcelStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ParcelJpaRepository : JpaRepository<ParcelEntity, UUID> {
    fun findByTrackingNumber(trackingNumber: String): ParcelEntity?

    fun findBySellerIdOrderByRegisteredAtDesc(sellerId: Long, pageable: Pageable): List<ParcelEntity>

    fun findBySellerIdAndLateDetectedAtIsNotNullOrderByLateDetectedAtDesc(sellerId: Long, pageable: Pageable): List<ParcelEntity>

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
