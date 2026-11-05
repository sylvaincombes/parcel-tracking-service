package com.shoplane.parceltracking.domain.parcel

import java.math.BigDecimal

data class ParcelDimensions(
    val weightKg: BigDecimal,
    val lengthCm: BigDecimal,
    val widthCm: BigDecimal,
    val heightCm: BigDecimal,
) {
    init {
        require(listOf(weightKg, lengthCm, widthCm, heightCm).all { it.signum() > 0 }) {
            "Parcel dimensions must be positive"
        }
    }
}
