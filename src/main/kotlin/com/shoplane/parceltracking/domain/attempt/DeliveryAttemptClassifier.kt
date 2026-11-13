package com.shoplane.parceltracking.domain.attempt

enum class DeliveryAttemptReason {
    REFUSED_BY_RECIPIENT,
    ADDRESS_ISSUE,
    BUSINESS_CLOSED,
    CUSTOMER_NOT_HOME,
    OTHER,
}

class DeliveryAttemptClassifier {
    private val rules: List<Rule> =
        listOf(
            Rule(DeliveryAttemptReason.REFUSED_BY_RECIPIENT, "refused", "refusé", "declined by recipient"),
            Rule(DeliveryAttemptReason.ADDRESS_ISSUE, "address", "adresse", "postal code", "zip code", "unknown street"),
            Rule(DeliveryAttemptReason.BUSINESS_CLOSED, "closed", "fermé", "business hours"),
            Rule(DeliveryAttemptReason.CUSTOMER_NOT_HOME, "absent", "not at home", "nobody", "no one home"),
        )

    fun classify(carrierMessage: String): DeliveryAttemptReason {
        val text = carrierMessage.lowercase()
        return rules.firstOrNull { it.matches(text) }?.reason ?: DeliveryAttemptReason.OTHER
    }

    private class Rule(val reason: DeliveryAttemptReason, vararg val keywords: String) {
        fun matches(text: String): Boolean = keywords.any { it in text }
    }
}
