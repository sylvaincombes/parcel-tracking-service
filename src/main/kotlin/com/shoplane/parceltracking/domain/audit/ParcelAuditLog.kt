package com.shoplane.parceltracking.domain.audit

import com.shoplane.parceltracking.domain.parcel.ParcelId
import java.time.Instant

enum class ParcelAuditAction { CANCELLED }

data class ParcelAuditEntry(
    val parcelId: ParcelId,
    val actorUserId: String,
    val action: ParcelAuditAction,
    val occurredAt: Instant,
)

interface ParcelAuditLog {
    fun record(entry: ParcelAuditEntry)

    fun entriesOf(parcelId: ParcelId): List<ParcelAuditEntry>
}
