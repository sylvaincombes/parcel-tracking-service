package com.shoplane.parceltracking.infra.persistence

import com.shoplane.parceltracking.domain.attempt.DeliveryAttemptReason
import com.shoplane.parceltracking.domain.parcel.TrackingEventType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "tracking_event")
class TrackingEventEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parcel_id", nullable = false, updatable = false)
    val parcel: ParcelEntity,
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    val type: TrackingEventType,
    @Column(name = "occurred_at", nullable = false, updatable = false)
    val occurredAt: Instant,
    @Column(name = "location", updatable = false)
    val location: String?,
    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_reason", updatable = false)
    val attemptReason: DeliveryAttemptReason?,
    @Column(name = "received_at", nullable = false, updatable = false)
    val receivedAt: Instant,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
