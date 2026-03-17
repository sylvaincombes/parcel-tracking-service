package com.shoplane.parceltracking.infra.outbox

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "parcel-tracking.outbox.topics")
data class OutboxTopics(
    val parcelEvents: String,
)
