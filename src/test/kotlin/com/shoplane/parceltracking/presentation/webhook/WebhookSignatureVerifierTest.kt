package com.shoplane.parceltracking.presentation.webhook

import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class WebhookSignatureVerifierTest {
    private val verifier = WebhookSignatureVerifier(WebhookProperties(mapOf("colisway" to "colisway-webhook-secret")))
    private val body = """{"eventId":"evt-88120","trackingNumber":"CW4400112233FR","code":"DLV"}"""

    @Test
    fun `accepts a body signed with the carrier secret`() {
        val signature = WebhookSignatureVerifier.sign("colisway-webhook-secret", body)

        assertThatCode { verifier.verify("colisway", body, signature.uppercase()) }.doesNotThrowAnyException()
    }

    @Test
    fun `rejects a tampered body`() {
        val signature = WebhookSignatureVerifier.sign("colisway-webhook-secret", body)

        assertThatThrownBy { verifier.verify("colisway", body.replace("DLV", "RTS"), signature) }
            .isInstanceOf(InvalidWebhookSignatureException::class.java)
    }

    @Test
    fun `rejects a carrier without a configured secret`() {
        val signature = WebhookSignatureVerifier.sign("colisway-webhook-secret", body)

        assertThatThrownBy { verifier.verify("parcelwave", body, signature) }
            .isInstanceOf(InvalidWebhookSignatureException::class.java)
    }

    @Test
    fun `rejects a request without signature`() {
        assertThatThrownBy { verifier.verify("colisway", body, null) }
            .isInstanceOf(InvalidWebhookSignatureException::class.java)
    }
}
