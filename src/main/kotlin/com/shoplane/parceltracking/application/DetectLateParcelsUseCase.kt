package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.domain.carrier.CarrierDirectory
import com.shoplane.parceltracking.domain.event.ParcelEventPublisher
import com.shoplane.parceltracking.domain.event.ParcelLateDetected
import com.shoplane.parceltracking.domain.lateness.LateParcelDetector
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock
import java.time.LocalDate

@Service
class DetectLateParcelsUseCase(
    private val parcels: ParcelRepository,
    private val carriers: CarrierDirectory,
    private val detector: LateParcelDetector,
    private val publisher: ParcelEventPublisher,
    private val transactionTemplate: TransactionTemplate,
    private val clock: Clock,
) {
    fun detect(batchSize: Int = DEFAULT_BATCH_SIZE): Int {
        val today = LocalDate.now(clock)
        var flagged = 0
        var batch = parcels.findInFlightPickedUp(after = null, limit = batchSize)
        while (batch.isNotEmpty()) {
            flagged += batch.count { parcel -> transactionTemplate.execute { flagIfLate(parcel, today) } == true }
            batch = parcels.findInFlightPickedUp(after = batch.last().id, limit = batchSize)
        }
        logger.info("Late parcel detection flagged {} parcels", flagged)
        return flagged
    }

    private fun flagIfLate(parcel: Parcel, today: LocalDate): Boolean {
        val pickedUpOn = requireNotNull(parcel.pickedUpOn)
        val carrier = carriers.findCarrier(parcel.carrierCode)
        if (carrier == null) {
            logger.warn(
                "Cannot check lateness of parcel {}: carrier {} is missing from the registry",
                parcel.id,
                parcel.carrierCode,
            )
            return false
        }
        if (!detector.isLate(parcel, carrier, today)) {
            return false
        }
        parcel.markLate(clock.instant())
        parcels.save(parcel)
        publisher.publish(
            ParcelLateDetected(
                parcelId = parcel.id,
                trackingNumber = parcel.trackingNumber,
                sellerId = parcel.sellerId,
                deliveryDeadline = detector.deliveryDeadline(pickedUpOn, carrier),
                occurredAt = clock.instant(),
            ),
        )
        return true
    }

    private companion object {
        const val DEFAULT_BATCH_SIZE = 500
        val logger = LoggerFactory.getLogger(DetectLateParcelsUseCase::class.java)
    }
}
