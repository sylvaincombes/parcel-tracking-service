package com.shoplane.parceltracking.presentation.webhook

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RecentCarrierEventsTest {
    private val recentEvents = RecentCarrierEvents()

    @Test
    fun `the first delivery of a carrier event is new`() {
        assertThat(recentEvents.isFirstDelivery("colisway", "evt-88120")).isTrue()
    }

    @Test
    fun `a repeated delivery of the same carrier event is not new`() {
        recentEvents.isFirstDelivery("colisway", "evt-88120")

        assertThat(recentEvents.isFirstDelivery("colisway", "evt-88120")).isFalse()
    }

    @Test
    fun `the same event id from another carrier is a different event`() {
        recentEvents.isFirstDelivery("colisway", "evt-88120")

        assertThat(recentEvents.isFirstDelivery("parcelwave", "evt-88120")).isTrue()
    }

    @Test
    fun `only the most recent carrier events are remembered`() {
        recentEvents.isFirstDelivery("colisway", "evt-88120")
        repeat(RecentCarrierEvents.MAX_REMEMBERED_EVENTS) { index ->
            recentEvents.isFirstDelivery("colisway", "evt-${90000 + index}")
        }

        assertThat(recentEvents.isFirstDelivery("colisway", "evt-88120")).isTrue()
    }
}
