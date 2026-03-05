package com.shoplane.parceltracking.fixtures

import com.shoplane.parceltracking.domain.parcel.PageRequest
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import com.shoplane.parceltracking.domain.parcel.SellerId
import com.shoplane.parceltracking.domain.parcel.TrackingNumber

class InMemoryParcelRepository(vararg initial: Parcel) : ParcelRepository {
    private val parcels = linkedMapOf<ParcelId, Parcel>()

    init {
        initial.forEach(::save)
    }

    val saved: List<Parcel>
        get() = parcels.values.toList()

    override fun findById(id: ParcelId): Parcel? = parcels[id]?.let(::copyOf)

    override fun findByTrackingNumber(trackingNumber: TrackingNumber): Parcel? =
        parcels.values.firstOrNull { it.trackingNumber == trackingNumber }?.let(::copyOf)

    override fun findBySeller(sellerId: SellerId, page: PageRequest): List<Parcel> =
        parcels.values
            .filter { it.sellerId == sellerId }
            .sortedByDescending { it.registeredAt }
            .drop(page.page * page.size)
            .take(page.size)
            .map(::copyOf)

    override fun findInFlightPickedUp(after: ParcelId?, limit: Int): List<Parcel> =
        parcels.values
            .filter { it.pickedUpOn != null && !it.status.isTerminal && !it.isLate }
            .sortedBy { it.id.value }
            .filter { after == null || it.id.value > after.value }
            .take(limit)
            .map(::copyOf)

    override fun save(parcel: Parcel): Parcel {
        val next = copyOf(parcel, version = (parcel.version ?: -1) + 1)
        parcels[parcel.id] = next
        return copyOf(next)
    }

    private fun copyOf(parcel: Parcel, version: Long? = parcel.version) =
        Parcel(
            id = parcel.id,
            trackingNumber = parcel.trackingNumber,
            carrierCode = parcel.carrierCode,
            sellerId = parcel.sellerId,
            orderReference = parcel.orderReference,
            registeredAt = parcel.registeredAt,
            status = parcel.status,
            deliveryInstructions = parcel.deliveryInstructions,
            events = parcel.events,
            pickedUpOn = parcel.pickedUpOn,
            deliveredAt = parcel.deliveredAt,
            lateDetectedAt = parcel.lateDetectedAt,
            version = version,
        )
}
