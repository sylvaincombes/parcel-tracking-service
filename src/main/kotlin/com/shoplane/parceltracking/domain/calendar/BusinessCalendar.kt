package com.shoplane.parceltracking.domain.calendar

import java.time.DayOfWeek
import java.time.LocalDate

class BusinessCalendar(private val publicHolidays: Set<LocalDate>) {
    fun isBusinessDay(date: LocalDate): Boolean = date.dayOfWeek !in WEEKEND && date !in publicHolidays

    fun addBusinessDays(start: LocalDate, businessDays: Int): LocalDate {
        require(businessDays >= 0) { "Business days must not be negative, got $businessDays" }
        var date = start
        var remaining = businessDays
        while (remaining > 0) {
            date = date.plusDays(1)
            if (isBusinessDay(date)) {
                remaining--
            }
        }
        return date
    }

    private companion object {
        val WEEKEND = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    }
}
