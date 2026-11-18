package com.shoplane.parceltracking.domain.transit

import com.shoplane.parceltracking.domain.calendar.BusinessCalendar
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelStatus
import java.time.LocalDate
import java.time.ZoneOffset

class TransitTimeCalculator(private val calendar: BusinessCalendar) {
    fun businessDaysInTransit(parcel: Parcel): Int? {
        if (parcel.status != ParcelStatus.DELIVERED) {
            return null
        }
        val pickedUpOn = parcel.pickedUpOn!!
        val deliveredOn = LocalDate.ofInstant(parcel.deliveredAt!!, ZoneOffset.UTC)
        return calendar.businessDaysBetween(pickedUpOn, deliveredOn)
    }
}
