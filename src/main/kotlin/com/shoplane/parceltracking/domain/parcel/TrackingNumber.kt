package com.shoplane.parceltracking.domain.parcel

@JvmInline
value class TrackingNumber private constructor(val value: String) {
    override fun toString(): String = value

    companion object {
        private val FORMAT = Regex("^[A-Z0-9]{10,30}$")

        fun of(raw: String): TrackingNumber {
            val normalized = raw.filterNot { it.isWhitespace() || it == '-' }.uppercase()
            require(FORMAT.matches(normalized)) {
                "Invalid tracking number: '$raw'. Tracking numbers have 10 to 30 letters or digits."
            }
            return TrackingNumber(normalized)
        }
    }
}
