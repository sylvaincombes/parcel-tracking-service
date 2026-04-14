package com.shoplane.parceltracking.infra.persistence

import com.shoplane.parceltracking.domain.parcel.ParcelStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "parcel")
class ParcelEntity(
    @Id
    val id: UUID,
    @Column(name = "tracking_number", nullable = false, unique = true, updatable = false)
    val trackingNumber: String,
    @Column(name = "carrier_code", nullable = false, updatable = false)
    val carrierCode: String,
    @Column(name = "seller_id", nullable = false, updatable = false)
    val sellerId: Long,
    @Column(name = "order_reference", nullable = false, updatable = false)
    val orderReference: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ParcelStatus,
    @Column(name = "delivery_instructions")
    var deliveryInstructions: String?,
    @Column(name = "picked_up_on")
    var pickedUpOn: LocalDate?,
    @Column(name = "delivered_at")
    var deliveredAt: Instant?,
    @Column(name = "late_detected_at")
    var lateDetectedAt: Instant?,
    @Column(name = "registered_at", nullable = false, updatable = false)
    val registeredAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
    @Version
    @Column(name = "version", nullable = false)
    var version: Long? = null,
) {
    @OneToMany(mappedBy = "parcel", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("id")
    val events: MutableList<TrackingEventEntity> = mutableListOf()
}
