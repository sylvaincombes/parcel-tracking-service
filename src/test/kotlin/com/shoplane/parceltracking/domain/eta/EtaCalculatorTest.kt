package com.shoplane.parceltracking.domain.eta

import com.shoplane.parceltracking.domain.calendar.BusinessCalendar
import com.shoplane.parceltracking.domain.carrier.TransitDays
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EtaCalculatorTest {
    private val calculator = EtaCalculator(BusinessCalendar(setOf(LocalDate.parse("2026-11-11"))))

    @Test
    fun `estimates a delivery window from the carrier transit days`() {
        val estimate = calculator.estimate(LocalDate.parse("2026-11-16"), TransitDays(min = 1, max = 2))

        assertThat(estimate).isEqualTo(DeliveryEstimate(LocalDate.parse("2026-11-17"), LocalDate.parse("2026-11-18")))
    }

    @Test
    fun `a friday pick-up is delivered the following week`() {
        val estimate = calculator.estimate(LocalDate.parse("2026-11-13"), TransitDays(min = 1, max = 3))

        assertThat(estimate).isEqualTo(DeliveryEstimate(LocalDate.parse("2026-11-16"), LocalDate.parse("2026-11-18")))
    }

    @Test
    fun `the delivery window moves past a public holiday`() {
        val estimate = calculator.estimate(LocalDate.parse("2026-11-10"), TransitDays(min = 1, max = 2))

        assertThat(estimate).isEqualTo(DeliveryEstimate(LocalDate.parse("2026-11-12"), LocalDate.parse("2026-11-13")))
    }
}
