package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.audit.ParcelAuditAction
import com.shoplane.parceltracking.domain.audit.ParcelAuditEntry
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.fixtures.InMemoryParcelAuditLog
import com.shoplane.parceltracking.fixtures.InMemoryParcelRepository
import com.shoplane.parceltracking.fixtures.LIGHTING_SELLER
import com.shoplane.parceltracking.fixtures.aRegisteredParcel
import com.shoplane.parceltracking.fixtures.aSellerAdmin
import com.shoplane.parceltracking.fixtures.aSellerViewer
import com.shoplane.parceltracking.fixtures.aSupportAgent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant

class GetParcelAuditTrailUseCaseTest {
    private val parcel = aRegisteredParcel()
    private val cancellation = ParcelAuditEntry(
        parcelId = parcel.id,
        actorUserId = "seller-admin-4101",
        action = ParcelAuditAction.CANCELLED,
        occurredAt = Instant.parse("2026-11-02T10:00:00Z"),
    )
    private val auditLog = InMemoryParcelAuditLog().apply { record(cancellation) }
    private val useCase = GetParcelAuditTrailUseCase(InMemoryParcelRepository(parcel), auditLog, ParcelAccessPolicy())

    @Test
    fun `a seller user reads the audit trail of its parcel`() {
        assertThat(useCase.auditTrail(aSellerViewer(), parcel.id)).containsExactly(cancellation)
    }

    @Test
    fun `another seller cannot read the audit trail of the parcel`() {
        assertThatThrownBy { useCase.auditTrail(aSellerAdmin(LIGHTING_SELLER), parcel.id) }
            .isInstanceOf(AccessDeniedException::class.java)
    }

    @Test
    fun `support agents read the audit trail of any parcel`() {
        assertThat(useCase.auditTrail(aSupportAgent(), parcel.id)).containsExactly(cancellation)
    }

    @Test
    fun `the audit trail of an unknown parcel is not found`() {
        assertThatThrownBy { useCase.auditTrail(aSupportAgent(), ParcelId.generate()) }
            .isInstanceOf(ParcelNotFoundException::class.java)
    }
}
