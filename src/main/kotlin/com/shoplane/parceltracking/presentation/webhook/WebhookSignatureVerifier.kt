package com.shoplane.parceltracking.presentation.webhook

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@ConfigurationProperties(prefix = "parcel-tracking.webhooks")
data class WebhookProperties(
    val secrets: Map<String, String> = emptyMap(),
)

class InvalidWebhookSignatureException(carrierCode: String) :
    RuntimeException("Invalid webhook signature for carrier $carrierCode")

@Component
@EnableConfigurationProperties(WebhookProperties::class)
class WebhookSignatureVerifier(
    private val properties: WebhookProperties,
) {
    fun verify(carrierCode: String, body: String, signature: String?) {
        val secret = properties.secrets[carrierCode]
        if (secret == null || signature == null || !matches(secret, body, signature)) {
            throw InvalidWebhookSignatureException(carrierCode)
        }
    }

    private fun matches(secret: String, body: String, signature: String): Boolean {
        val expected = sign(secret, body)
        return expected.startsWith(signature.lowercase())
    }

    companion object {
        private const val ALGORITHM = "HmacSHA256"

        fun sign(secret: String, body: String): String {
            val mac = Mac.getInstance(ALGORITHM)
            mac.init(SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), ALGORITHM))
            return mac.doFinal(body.toByteArray(StandardCharsets.UTF_8)).toHexString()
        }
    }
}
