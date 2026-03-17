package com.shoplane.parceltracking.infra.outbox

import org.springframework.data.jpa.repository.JpaRepository

interface OutboxMessageRepository : JpaRepository<OutboxMessageEntity, Long> {
    fun findTop100ByPublishedAtIsNullOrderByIdAsc(): List<OutboxMessageEntity>
}
