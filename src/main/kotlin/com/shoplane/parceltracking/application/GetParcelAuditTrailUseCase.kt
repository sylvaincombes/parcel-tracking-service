package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.audit.ParcelAuditEntry
import com.shoplane.parceltracking.domain.audit.ParcelAuditLog
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetParcelAuditTrailUseCase(
    private val parcels: ParcelRepository,
    private val auditLog: ParcelAuditLog,
    private val accessPolicy: ParcelAccessPolicy,
) {
    @Transactional(readOnly = true)
    fun auditTrail(actor: BackOfficeActor, parcelId: ParcelId): List<ParcelAuditEntry> {
        val parcel = parcels.findById(parcelId) ?: throw ParcelNotFoundException(parcelId.toString())
        accessPolicy.requireCanView(actor, parcel)
        return auditLog.entriesOf(parcel.id)
    }
}
