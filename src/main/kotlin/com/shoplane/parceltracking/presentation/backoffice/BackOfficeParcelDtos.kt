package com.shoplane.parceltracking.presentation.backoffice

import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.TrackingEvent
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.time.LocalDate

data class RegisterParcelRequest(
    @field:NotBlank
    val trackingNumber: String,
    @field:NotBlank
    val carrierCode: String,
    @field:NotBlank
    @field:Size(max = 64)
    val orderReference: String,
    @field:Size(max = 500)
    val deliveryInstructions: String? = null,
)

data class ParcelSummaryResponse(
    val parcelId: String,
    val trackingNumber: String,
    val carrierCode: String,
    val orderReference: String,
    val status: String,
    val late: Boolean,
    val registeredAt: Instant,
) {
    companion object {
        fun from(parcel: Parcel) =
            ParcelSummaryResponse(
                parcelId = parcel.id.toString(),
                trackingNumber = parcel.trackingNumber.value,
                carrierCode = parcel.carrierCode.value,
                orderReference = parcel.orderReference.value,
                status = parcel.status.name,
                late = parcel.isLate,
                registeredAt = parcel.registeredAt,
            )
    }
}

data class ParcelDetailResponse(
    val parcelId: String,
    val trackingNumber: String,
    val carrierCode: String,
    val sellerId: Long,
    val orderReference: String,
    val status: String,
    val deliveryInstructions: String?,
    val pickedUpOn: LocalDate?,
    val deliveredAt: Instant?,
    val transitBusinessDays: Int?,
    val lateDetectedAt: Instant?,
    val registeredAt: Instant,
    val events: List<ParcelEventResponse>,
) {
    companion object {
        fun from(parcel: Parcel, transitBusinessDays: Int? = null) =
            ParcelDetailResponse(
                parcelId = parcel.id.toString(),
                trackingNumber = parcel.trackingNumber.value,
                carrierCode = parcel.carrierCode.value,
                sellerId = parcel.sellerId.value,
                orderReference = parcel.orderReference.value,
                status = parcel.status.name,
                deliveryInstructions = parcel.deliveryInstructions,
                pickedUpOn = parcel.pickedUpOn,
                deliveredAt = parcel.deliveredAt,
                transitBusinessDays = transitBusinessDays,
                lateDetectedAt = parcel.lateDetectedAt,
                registeredAt = parcel.registeredAt,
                events = parcel.timeline().map(ParcelEventResponse::from),
            )
    }
}

data class ParcelEventResponse(
    val type: String,
    val occurredAt: Instant,
    val location: String?,
    val attemptReason: String?,
) {
    companion object {
        fun from(event: TrackingEvent) =
            ParcelEventResponse(
                type = event.type.name,
                occurredAt = event.occurredAt,
                location = event.location,
                attemptReason = event.attemptReason?.name,
            )
    }
}
