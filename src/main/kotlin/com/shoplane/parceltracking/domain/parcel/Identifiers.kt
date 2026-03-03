package com.shoplane.parceltracking.domain.parcel

import java.util.UUID

@JvmInline
value class ParcelId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        fun generate(): ParcelId = ParcelId(UUID.randomUUID())
    }
}

@JvmInline
value class SellerId(val value: Long) {
    init {
        require(value > 0) { "Seller id must be positive, got $value" }
    }

    override fun toString(): String = value.toString()
}

@JvmInline
value class OrderReference(val value: String) {
    init {
        require(value.isNotBlank()) { "Order reference must not be blank" }
    }

    override fun toString(): String = value
}
