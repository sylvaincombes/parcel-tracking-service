package com.shoplane.parceltracking.domain.parcel

enum class ParcelStatus {
    REGISTERED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERY_FAILED,
    DELIVERED,
    RETURNED_TO_SENDER,
    CANCELLED,
    ;

    val isTerminal: Boolean
        get() = this == DELIVERED || this == RETURNED_TO_SENDER || this == CANCELLED

    val acceptsDeliveryInstructions: Boolean
        get() = this == REGISTERED || this == PICKED_UP || this == IN_TRANSIT || this == DELIVERY_FAILED

    fun canMoveTo(next: ParcelStatus): Boolean = next in allowedNextStatuses()

    private fun allowedNextStatuses(): Set<ParcelStatus> =
        when (this) {
            REGISTERED -> setOf(PICKED_UP, IN_TRANSIT, CANCELLED)
            PICKED_UP -> setOf(IN_TRANSIT, OUT_FOR_DELIVERY)
            IN_TRANSIT -> setOf(IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERY_FAILED, RETURNED_TO_SENDER)
            OUT_FOR_DELIVERY -> setOf(IN_TRANSIT, DELIVERED, DELIVERY_FAILED)
            DELIVERY_FAILED -> setOf(IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, RETURNED_TO_SENDER)
            DELIVERED, RETURNED_TO_SENDER, CANCELLED -> emptySet()
        }
}
