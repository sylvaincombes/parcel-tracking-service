package com.shoplane.parceltracking.domain.calendar

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BusinessCalendarTest {
    private val armisticeDay = LocalDate.parse("2026-11-11")
    private val calendar = BusinessCalendar(setOf(armisticeDay))

    @Test
    fun `weekends and public holidays are not business days`() {
        assertThat(calendar.isBusinessDay(LocalDate.parse("2026-11-14"))).isFalse()
        assertThat(calendar.isBusinessDay(LocalDate.parse("2026-11-15"))).isFalse()
        assertThat(calendar.isBusinessDay(armisticeDay)).isFalse()
        assertThat(calendar.isBusinessDay(LocalDate.parse("2026-11-16"))).isTrue()
    }

    @Test
    fun `adding zero business days returns the start date`() {
        assertThat(calendar.addBusinessDays(LocalDate.parse("2026-11-16"), 0)).isEqualTo(LocalDate.parse("2026-11-16"))
    }

    @Test
    fun `adding one business day on a monday lands on tuesday`() {
        assertThat(calendar.addBusinessDays(LocalDate.parse("2026-11-16"), 1)).isEqualTo(LocalDate.parse("2026-11-17"))
    }

    @Test
    fun `adding business days skips the weekend`() {
        assertThat(calendar.addBusinessDays(LocalDate.parse("2026-11-13"), 1)).isEqualTo(LocalDate.parse("2026-11-16"))
    }

    @Test
    fun `adding business days skips public holidays`() {
        assertThat(calendar.addBusinessDays(LocalDate.parse("2026-11-09"), 3)).isEqualTo(LocalDate.parse("2026-11-13"))
    }

    @Test
    fun `counting from a saturday starts on the next monday`() {
        assertThat(calendar.addBusinessDays(LocalDate.parse("2026-11-14"), 1)).isEqualTo(LocalDate.parse("2026-11-16"))
    }

    @Test
    fun `rejects a negative number of business days`() {
        assertThatIllegalArgumentException().isThrownBy { calendar.addBusinessDays(LocalDate.parse("2026-11-16"), -1) }
    }
}
