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

    @Test
    fun `accepts the shortest carrier format`() {
        assertThat(TrackingNumber.of("1Z9A77X0V2").value).isEqualTo("1Z9A77X0V2")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "CW44", "CW4400112", "CW4400112233FR/../admin", "CW44001122#33FR", "CW440011223344556677889900112233"])
    fun `rejects malformed tracking numbers`(raw: String) {
        assertThatIllegalArgumentException().isThrownBy { TrackingNumber.of(raw) }
    }

    @Test
    fun `explains the expected format when rejecting a partial tracking number`() {
        assertThatIllegalArgumentException()
            .isThrownBy { TrackingNumber.of("44001122") }
            .withMessageContaining("10 to 30 letters or digits")
    }
}
