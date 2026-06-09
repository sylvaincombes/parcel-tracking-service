package com.shoplane.parceltracking.domain.carrier

import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import java.net.URI

@JvmInline
value class CarrierCode(val value: String) {
    init {
        require(FORMAT.matches(value)) { "Invalid carrier code: '$value'" }
    }

    override fun toString(): String = value

    private companion object {
        val FORMAT = Regex("^[a-z][a-z0-9-]{1,31}$")
    }
}

enum class CarrierStatus { ACTIVE, RETIRED }

data class TransitDays(val min: Int, val max: Int) {
    init {
        require(min in 0..max) { "Invalid transit days range $min..$max" }
    }
}

data class Carrier(
    val code: CarrierCode,
    val displayName: String,
    val trackingUrlTemplate: String,
    val transitDays: TransitDays,
    val status: CarrierStatus,
    val replacedBy: CarrierCode? = null,
) {
    val isActive: Boolean
        get() = status == CarrierStatus.ACTIVE

    fun trackingLinkFor(trackingNumber: TrackingNumber): URI =
        URI.create(trackingUrlTemplate.replace(TRACKING_NUMBER_PLACEHOLDER, trackingNumber.value))

    private companion object {
        const val TRACKING_NUMBER_PLACEHOLDER = "{trackingNumber}"
    }
}
