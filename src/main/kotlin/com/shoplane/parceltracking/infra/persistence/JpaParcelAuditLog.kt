package com.shoplane.parceltracking.infra.persistence

import com.shoplane.parceltracking.domain.audit.ParcelAuditEntry
import com.shoplane.parceltracking.domain.audit.ParcelAuditLog
import com.shoplane.parceltracking.domain.parcel.ParcelId
import org.springframework.stereotype.Repository

@Repository
class JpaParcelAuditLog(
    private val jpa: ParcelAuditEntryJpaRepository,
) : ParcelAuditLog {
    override fun record(entry: ParcelAuditEntry) {
        jpa.save(
            ParcelAuditEntryEntity(
                parcelId = entry.parcelId.value,
                actorUserId = entry.actorUserId,
                action = entry.action,
                occurredAt = entry.occurredAt,
            ),
        )
    }

    override fun entriesOf(parcelId: ParcelId): List<ParcelAuditEntry> =
        jpa.findByParcelIdOrderByOccurredAtDesc(parcelId.value).map { entity ->
            ParcelAuditEntry(
                parcelId = ParcelId(entity.parcelId),
                actorUserId = entity.actorUserId,
                action = entity.action,
                occurredAt = entity.occurredAt,
            )
        }
}
