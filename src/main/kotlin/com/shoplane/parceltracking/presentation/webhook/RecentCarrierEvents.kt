package com.shoplane.parceltracking.presentation.webhook

import org.springframework.stereotype.Component

@Component
class RecentCarrierEvents {
    private val seen = object : LinkedHashMap<CarrierEventKey, Unit>(INITIAL_CAPACITY, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<CarrierEventKey, Unit>): Boolean =
            size > MAX_REMEMBERED_EVENTS
    }

    fun isFirstDelivery(carrierCode: String, eventId: String): Boolean =
        seen.put(CarrierEventKey(carrierCode, eventId), Unit) == null

    private data class CarrierEventKey(val carrierCode: String, val eventId: String)

    companion object {
        const val MAX_REMEMBERED_EVENTS = 10_000
        private const val INITIAL_CAPACITY = 16
        private const val LOAD_FACTOR = 0.75f
    }
}
