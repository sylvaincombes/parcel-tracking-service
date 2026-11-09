package com.shoplane.parceltracking.fixtures

import com.shoplane.parceltracking.domain.audit.ParcelAuditEntry
import com.shoplane.parceltracking.domain.audit.ParcelAuditLog
import com.shoplane.parceltracking.domain.parcel.ParcelId

class InMemoryParcelAuditLog : ParcelAuditLog {
    private val entries = mutableListOf<ParcelAuditEntry>()

    override fun record(entry: ParcelAuditEntry) {
        entries += entry
    }

    override fun entriesOf(parcelId: ParcelId): List<ParcelAuditEntry> =
        entries.filter { it.parcelId == parcelId }.sortedByDescending { it.occurredAt }
}
