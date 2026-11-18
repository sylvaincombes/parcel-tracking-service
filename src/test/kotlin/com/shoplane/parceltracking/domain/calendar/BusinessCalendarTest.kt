package com.shoplane.parceltracking.domain.calendar

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Nested
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
    fun `counts the business days after the start date up to the end date`() {
        assertThat(calendar.businessDaysBetween(LocalDate.parse("2026-11-16"), LocalDate.parse("2026-11-18"))).isEqualTo(2)
    }

    @Test
    fun `counting business days between two dates skips weekends and public holidays`() {
        assertThat(calendar.businessDaysBetween(LocalDate.parse("2026-11-10"), LocalDate.parse("2026-11-16"))).isEqualTo(3)
    }

    @Test
    fun `there are no business days between a date and itself`() {
        assertThat(calendar.businessDaysBetween(LocalDate.parse("2026-11-16"), LocalDate.parse("2026-11-16"))).isZero()
    }

    @Test
    fun `rejects a negative number of business days`() {
        assertThatIllegalArgumentException().isThrownBy { calendar.addBusinessDays(LocalDate.parse("2026-11-16"), -1) }
    }

    @Nested
    inner class NationalCalendarOnly {
        @Test
        fun `regional public holidays are business days`() {
            val goodFriday = LocalDate.parse("2026-04-03")
            val saintStephensDay = LocalDate.parse("2025-12-26")

            assertThat(calendar.isBusinessDay(goodFriday)).isTrue()
            assertThat(calendar.isBusinessDay(saintStephensDay)).isTrue()
        }

        @Test
        fun `saturday is not a business day, even for carriers that deliver on saturdays`() {
            assertThat(calendar.isBusinessDay(LocalDate.parse("2026-11-21"))).isFalse()
        }

        @Test
        fun `only configured dates are public holidays`() {
            assertThat(calendar.isBusinessDay(LocalDate.parse("2027-01-01"))).isTrue()
        }
    }
}
