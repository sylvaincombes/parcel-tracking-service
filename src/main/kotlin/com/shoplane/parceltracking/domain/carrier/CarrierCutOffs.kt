package com.shoplane.parceltracking.domain.carrier

import java.time.LocalDate
import java.time.LocalTime

interface CarrierCutOffs {
    fun cutOffFor(carrier: CarrierCode, day: LocalDate): LocalTime?
}
