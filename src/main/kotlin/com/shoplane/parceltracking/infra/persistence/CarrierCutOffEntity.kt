package com.shoplane.parceltracking.infra.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalTime

@Entity
@Table(name = "carrier_cut_off")
class CarrierCutOffEntity(
    @Id
    @Column(name = "carrier_code")
    val carrierCode: String,
    @Column(name = "cut_off_time", nullable = false)
    val cutOffTime: LocalTime,
)
