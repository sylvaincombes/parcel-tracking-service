package com.shoplane.parceltracking.domain.parcel

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class TrackingNumberTest {
    @Test
    fun `removes spaces and dashes and upper-cases the tracking number`() {
        assertThat(TrackingNumber.of(" cw44-0011 2233fr ").value).isEqualTo("CW4400112233FR")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "CW44", "CW4400112233FR/../admin", "CW44001122#33FR", "CW440011223344556677889900112233"])
    fun `rejects malformed tracking numbers`(raw: String) {
        assertThatIllegalArgumentException().isThrownBy { TrackingNumber.of(raw) }
    }
}
