package com.shoplane.parceltracking.domain.attempt

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class DeliveryAttemptClassifierTest {
    private val classifier = DeliveryAttemptClassifier()

    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = [
            "Parcel refused by recipient           | REFUSED_BY_RECIPIENT",
            "Colis refusé par le destinataire      | REFUSED_BY_RECIPIENT",
            "Address incomplete, missing building  | ADDRESS_ISSUE",
            "Adresse introuvable                   | ADDRESS_ISSUE",
            "Shop closed during delivery window    | BUSINESS_CLOSED",
            "Commerce fermé                        | BUSINESS_CLOSED",
            "Recipient absent, notice left         | CUSTOMER_NOT_HOME",
            "Nobody answered the door              | CUSTOMER_NOT_HOME",
            "Vehicle breakdown                     | OTHER",
            "''                                    | OTHER",
        ],
    )
    fun `classifies carrier messages by keyword`(message: String, expected: DeliveryAttemptReason) {
        assertThat(classifier.classify(message)).isEqualTo(expected)
    }

    @Nested
    inner class KnownLimitations {
        @ParameterizedTest
        @CsvSource(
            delimiter = '|',
            value = [
                "Address checked, recipient absent              | ADDRESS_ISSUE",
                "Recipient not absent, parcel left with neighbour | CUSTOMER_NOT_HOME",
                "Empfänger nicht angetroffen                    | OTHER",
                "Recipient abscent                              | OTHER",
            ],
        )
        fun `first matching rule wins, negations, other languages and typos are not understood`(
            message: String,
            expected: DeliveryAttemptReason,
        ) {
            assertThat(classifier.classify(message)).isEqualTo(expected)
        }
    }
}
