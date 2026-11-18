package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.carrier.CarrierDirectory
import com.shoplane.parceltracking.domain.carrier.UnknownCarrierException
import com.shoplane.parceltracking.domain.event.ParcelEventPublisher
import com.shoplane.parceltracking.domain.event.ParcelLateDetected
import com.shoplane.parceltracking.domain.lateness.LateParcelDetector
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.ParcelNotLateException
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class ResendLateNoticeUseCase(
    private val parcels: ParcelRepository,
    private val carriers: CarrierDirectory,
    private val accessPolicy: ParcelAccessPolicy,
    private val detector: LateParcelDetector,
    private val publisher: ParcelEventPublisher,
    private val clock: Clock,
) {
    @Transactional
    fun resend(actor: BackOfficeActor, parcelId: ParcelId) {
        val parcel = parcels.findById(parcelId) ?: throw ParcelNotFoundException(parcelId.toString())
        accessPolicy.requireCanManage(actor, parcel)
        if (!actor.isSupportAgent) {
            throw AccessDeniedException("User ${actor.userId} cannot resend late notices")
        }
        if (!parcel.isLate) {
            throw ParcelNotLateException(parcel.id)
        }
        val carrier = carriers.findCarrier(parcel.carrierCode) ?: throw UnknownCarrierException(parcel.carrierCode)
        publisher.publish(
            ParcelLateDetected(
                parcelId = parcel.id,
                trackingNumber = parcel.trackingNumber,
                sellerId = parcel.sellerId,
                deliveryDeadline = detector.deliveryDeadline(requireNotNull(parcel.pickedUpOn), carrier),
                occurredAt = clock.instant(),
            ),
        )
    }
}
