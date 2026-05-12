package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.carrier.CarrierDirectory
import com.shoplane.parceltracking.domain.carrier.RetiredCarrierException
import com.shoplane.parceltracking.domain.carrier.UnknownCarrierException
import com.shoplane.parceltracking.domain.parcel.DuplicateTrackingNumberException
import com.shoplane.parceltracking.domain.parcel.OrderReference
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

data class RegisterParcel(
    val trackingNumber: TrackingNumber,
    val carrierCode: CarrierCode,
    val orderReference: OrderReference,
    val deliveryInstructions: String?,
)

@Service
class RegisterParcelUseCase(
    private val parcels: ParcelRepository,
    private val carriers: CarrierDirectory,
    private val clock: Clock,
) {
    @Transactional
    fun register(actor: BackOfficeActor, command: RegisterParcel): Parcel {
        val sellerId = actor.sellerId?.takeIf { actor.canManageSellerParcels }
            ?: throw AccessDeniedException("User ${actor.userId} cannot register parcels")
        val carrier = carriers.findCarrier(command.carrierCode) ?: throw UnknownCarrierException(command.carrierCode)
        if (!carrier.isActive) {
            throw RetiredCarrierException(carrier.code)
        }
        if (parcels.findByTrackingNumber(command.trackingNumber) != null) {
            throw DuplicateTrackingNumberException(command.trackingNumber)
        }
        val parcel = Parcel.register(
            trackingNumber = command.trackingNumber,
            carrierCode = carrier.code,
            sellerId = sellerId,
            orderReference = command.orderReference,
            deliveryInstructions = command.deliveryInstructions,
            now = clock.instant(),
        )
        return parcels.save(parcel)
    }
}
