package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.audit.ParcelAuditAction
import com.shoplane.parceltracking.domain.audit.ParcelAuditEntry
import com.shoplane.parceltracking.domain.audit.ParcelAuditLog
import com.shoplane.parceltracking.domain.event.ParcelEventPublisher
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class CancelParcelUseCase(
    private val parcels: ParcelRepository,
    private val accessPolicy: ParcelAccessPolicy,
    private val publisher: ParcelEventPublisher,
    private val auditLog: ParcelAuditLog,
    private val clock: Clock,
) {
    @Transactional
    fun cancel(actor: BackOfficeActor, parcelId: ParcelId): Parcel {
        val parcel = parcels.findById(parcelId) ?: throw ParcelNotFoundException(parcelId.toString())
        accessPolicy.requireCanManage(actor, parcel)
        val cancellation = parcel.cancel(clock.instant())
        val saved = parcels.save(parcel)
        publisher.publish(cancellation)
        auditLog.record(
            ParcelAuditEntry(
                parcelId = parcel.id,
                actorUserId = actor.userId,
                action = ParcelAuditAction.CANCELLED,
                occurredAt = cancellation.occurredAt,
            ),
        )
        return saved
    }
}
