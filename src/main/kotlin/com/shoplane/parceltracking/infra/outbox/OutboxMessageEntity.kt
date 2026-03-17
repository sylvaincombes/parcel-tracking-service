package com.shoplane.parceltracking.infra.outbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "outbox_message")
class OutboxMessageEntity(
    @Column(name = "topic", nullable = false, updatable = false)
    val topic: String,
    @Column(name = "message_key", nullable = false, updatable = false)
    val messageKey: String,
    @Column(name = "event_type", nullable = false, updatable = false)
    val eventType: String,
    @Column(name = "payload", nullable = false, updatable = false)
    val payload: String,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(name = "published_at")
    var publishedAt: Instant? = null
}
