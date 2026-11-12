package com.shoplane.parceltracking.fixtures

import com.shoplane.parceltracking.domain.parcel.PageRequest
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import com.shoplane.parceltracking.domain.parcel.ParcelSnapshot
import com.shoplane.parceltracking.domain.parcel.ParcelStatus
import com.shoplane.parceltracking.domain.parcel.SellerId
import com.shoplane.parceltracking.domain.parcel.TrackingEvent
import com.shoplane.parceltracking.domain.parcel.TrackingEventType
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import org.springframework.orm.ObjectOptimisticLockingFailureException
import java.time.LocalDate
import java.time.ZoneOffset

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

    override fun findSnapshot(trackingNumber: TrackingNumber): ParcelSnapshot? =
        parcels.values.firstOrNull { it.trackingNumber == trackingNumber }?.let {
            ParcelSnapshot(it.id, it.trackingNumber, it.carrierCode, it.sellerId, it.status)
        }

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
        val stored = parcels[parcel.id]
        if (stored != null && stored.version != parcel.version) {
            throw ObjectOptimisticLockingFailureException(Parcel::class.java, parcel.id.value)
        }
        val next = copyOf(parcel, version = (parcel.version ?: -1) + 1)
        parcels[parcel.id] = next
        return copyOf(next)
    }

    override fun appendEvent(parcelId: ParcelId, event: TrackingEvent, status: ParcelStatus) {
        val stored = parcels.getValue(parcelId)
        val pickedUpOn = stored.pickedUpOn
            ?: LocalDate.ofInstant(event.occurredAt, ZoneOffset.UTC).takeIf { event.type == TrackingEventType.PICKED_UP }
        parcels[parcelId] = Parcel(
            id = stored.id,
            trackingNumber = stored.trackingNumber,
            carrierCode = stored.carrierCode,
            sellerId = stored.sellerId,
            orderReference = stored.orderReference,
            registeredAt = stored.registeredAt,
            status = status,
            deliveryInstructions = stored.deliveryInstructions,
            events = stored.events + event,
            pickedUpOn = pickedUpOn,
            deliveredAt = if (status == ParcelStatus.DELIVERED) event.occurredAt else stored.deliveredAt,
            lateDetectedAt = stored.lateDetectedAt,
            version = stored.version,
        )
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
