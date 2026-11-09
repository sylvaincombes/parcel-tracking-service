package com.shoplane.parceltracking.infra.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ParcelAuditEntryJpaRepository : JpaRepository<ParcelAuditEntryEntity, Long> {
    fun findByParcelIdOrderByOccurredAtDesc(parcelId: UUID): List<ParcelAuditEntryEntity>
}
