package com.shoplane.parceltracking.domain.parcel

data class ParcelDimensions(
    val weightGrams: Int,
    val lengthMm: Int,
    val widthMm: Int,
    val heightMm: Int,
) {
    init {
        require(listOf(weightGrams, lengthMm, widthMm, heightMm).all { it > 0 }) {
            "Parcel dimensions must be positive"
        }
    }
}
