package com.shoplane.parceltracking.infra.persistence

import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.carrier.CarrierCutOffs
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalTime

@Repository
class JpaCarrierCutOffs(
    private val jpa: CarrierCutOffJpaRepository,
) : CarrierCutOffs {
    override fun cutOffFor(carrier: CarrierCode, day: LocalDate): LocalTime? {
        val key = CarrierCutOffKey(carrierCode = carrier.value, weekday = day.dayOfWeek.value.toShort())
        return jpa.findById(key).map(CarrierCutOffEntity::cutOffTime).orElse(null)
    }
}
