package com.shoplane.parceltracking.presentation.webhook

import java.time.Instant

data class CarrierWebhookPayload(
    val eventId: String,
    val trackingNumber: String,
    val code: String,
    val occurredAt: Instant,
    val location: String? = null,
    val message: String? = null,
)

data class CarrierWebhookResponse(val outcome: String)
