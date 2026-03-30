package com.shoplane.parceltracking.domain.eta

import com.shoplane.parceltracking.domain.calendar.BusinessCalendar
import com.shoplane.parceltracking.domain.carrier.TransitDays
import java.time.LocalDate

data class DeliveryEstimate(val earliest: LocalDate, val latest: LocalDate)

class EtaCalculator(private val calendar: BusinessCalendar) {
    fun estimate(pickedUpOn: LocalDate, transitDays: TransitDays): DeliveryEstimate =
        DeliveryEstimate(
            earliest = calendar.addBusinessDays(pickedUpOn, transitDays.min),
            latest = calendar.addBusinessDays(pickedUpOn, transitDays.max),
        )
}
