package com.shoplane.parceltracking.infra.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalTime

@Embeddable
data class CarrierCutOffKey(
    @Column(name = "carrier_code")
    val carrierCode: String,
    @Column(name = "weekday")
    val weekday: Short,
) : Serializable

@Entity
@Table(name = "carrier_cut_off")
class CarrierCutOffEntity(
    @EmbeddedId
    val id: CarrierCutOffKey,
    @Column(name = "cut_off_time", nullable = false)
    val cutOffTime: LocalTime,
)
