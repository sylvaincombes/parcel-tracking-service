package com.shoplane.parceltracking.presentation.webhook

import com.shoplane.parceltracking.application.RecordTrackingEvent
import com.shoplane.parceltracking.application.RecordTrackingEventUseCase
import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.json.JsonMapper

@RestController
@RequestMapping("/webhooks/v1/carriers")
class CarrierWebhookController(
    private val signatureVerifier: WebhookSignatureVerifier,
    private val recordTrackingEvent: RecordTrackingEventUseCase,
    private val jsonMapper: JsonMapper,
) {
    @PostMapping("/{carrierCode}/events")
    fun receive(
        @PathVariable carrierCode: String,
        @RequestHeader(SIGNATURE_HEADER, required = false) signature: String?,
        @RequestBody body: String,
    ): ResponseEntity<CarrierWebhookResponse> {
        signatureVerifier.verify(carrierCode, body, signature)
        val payload = jsonMapper.readValue(body, CarrierWebhookPayload::class.java)
        val type = CarrierEventCodeMapper.typeOf(payload.code)
        if (type == null) {
            logger.info("Ignoring carrier event {} with unmapped code {} from {}", payload.eventId, payload.code, carrierCode)
            return ResponseEntity.accepted().body(CarrierWebhookResponse("IGNORED"))
        }
        val outcome = recordTrackingEvent.record(
            RecordTrackingEvent(
                carrierCode = CarrierCode(carrierCode),
                trackingNumber = TrackingNumber.of(payload.trackingNumber),
                type = type,
                occurredAt = payload.occurredAt,
                location = payload.location,
                carrierMessage = payload.message,
            ),
        )
        return ResponseEntity.accepted().body(CarrierWebhookResponse(outcome.name))
    }

    private companion object {
        const val SIGNATURE_HEADER = "X-Carrier-Signature"
        val logger = LoggerFactory.getLogger(CarrierWebhookController::class.java)
    }
}
