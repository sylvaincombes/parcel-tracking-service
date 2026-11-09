package com.shoplane.parceltracking.infra.persistence

import com.shoplane.parceltracking.domain.audit.ParcelAuditAction
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "parcel_audit_entry")
class ParcelAuditEntryEntity(
    @Column(name = "parcel_id", nullable = false, updatable = false)
    val parcelId: UUID,
    @Column(name = "actor_user_id", nullable = false, updatable = false)
    val actorUserId: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, updatable = false)
    val action: ParcelAuditAction,
    @Column(name = "occurred_at", nullable = false, updatable = false)
    val occurredAt: Instant,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
