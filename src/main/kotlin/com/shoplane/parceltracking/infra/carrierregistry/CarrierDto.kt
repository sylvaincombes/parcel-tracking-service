package com.shoplane.parceltracking.infra.carrierregistry

data class CarrierDto(
    val code: String,
    val displayName: String,
    val trackingUrlTemplate: String,
    val transitDays: TransitDaysDto,
    val status: CarrierStatusDto,
    val retiredOn: String? = null,
    val replacedBy: String? = null,
)

data class TransitDaysDto(
    val min: Int,
    val max: Int,
)

enum class CarrierStatusDto { ACTIVE, RETIRED }
