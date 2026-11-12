package com.shoplane.parceltracking.domain.parcel

enum class ParcelStatus {
    REGISTERED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERY_FAILED,
    AWAITING_COLLECTION,
    DELIVERED,
    RETURNED_TO_SENDER,
    CANCELLED,
    ;

    val isTerminal: Boolean
        get() = this == DELIVERED || this == RETURNED_TO_SENDER || this == CANCELLED

    fun canMoveTo(next: ParcelStatus): Boolean = next in allowedNextStatuses()

    private fun allowedNextStatuses(): Set<ParcelStatus> =
        when (this) {
            REGISTERED -> setOf(PICKED_UP, IN_TRANSIT, CANCELLED)
            PICKED_UP -> setOf(IN_TRANSIT, OUT_FOR_DELIVERY)
            IN_TRANSIT -> setOf(IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERY_FAILED, AWAITING_COLLECTION, RETURNED_TO_SENDER)
            OUT_FOR_DELIVERY -> setOf(IN_TRANSIT, DELIVERED, DELIVERY_FAILED, AWAITING_COLLECTION)
            DELIVERY_FAILED -> setOf(IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, AWAITING_COLLECTION, RETURNED_TO_SENDER)
            AWAITING_COLLECTION -> setOf(DELIVERED, RETURNED_TO_SENDER)
            DELIVERED, RETURNED_TO_SENDER, CANCELLED -> emptySet()
        }
}
