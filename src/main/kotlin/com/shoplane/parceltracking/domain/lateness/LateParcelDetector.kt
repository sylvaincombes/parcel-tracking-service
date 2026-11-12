package com.shoplane.parceltracking.domain.lateness

import com.shoplane.parceltracking.domain.calendar.BusinessCalendar
import com.shoplane.parceltracking.domain.carrier.Carrier
import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.parcel.Parcel
import java.time.LocalDate

class LateParcelDetector(
    private val calendar: BusinessCalendar,
    private val graceBusinessDays: Int,
    private val excludedCarriers: Set<CarrierCode> = emptySet(),
) {
    init {
        require(graceBusinessDays >= 0) { "Grace period must not be negative" }
    }

    fun deliveryDeadline(pickedUpOn: LocalDate, carrier: Carrier): LocalDate =
        calendar.addBusinessDays(pickedUpOn, carrier.transitDays.max + graceBusinessDays)

    fun isLate(parcel: Parcel, carrier: Carrier, today: LocalDate): Boolean {
        val pickedUpOn = parcel.pickedUpOn ?: return false
        if (parcel.status.isTerminal || parcel.isLate || carrier.code in excludedCarriers) {
            return false
        }
        return today.isAfter(deliveryDeadline(pickedUpOn, carrier))
    }
}
