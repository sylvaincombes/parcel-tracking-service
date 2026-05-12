package com.shoplane.parceltracking.presentation.backoffice

import com.shoplane.parceltracking.application.BackOfficeParcelQueries
import com.shoplane.parceltracking.application.RegisterParcel
import com.shoplane.parceltracking.application.RegisterParcelUseCase
import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.parcel.OrderReference
import com.shoplane.parceltracking.domain.parcel.PageRequest
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.SellerId
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/back-office/v1/parcels")
class BackOfficeParcelController(
    private val queries: BackOfficeParcelQueries,
    private val registerParcel: RegisterParcelUseCase,
) {
    @GetMapping
    fun list(
        actor: BackOfficeActor,
        @RequestParam(required = false) sellerId: Long?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
    ): List<ParcelSummaryResponse> =
        queries.list(actor, sellerId?.let(::SellerId), PageRequest(page, size)).map(ParcelSummaryResponse::from)

    @GetMapping("/{parcelId}")
    fun detail(actor: BackOfficeActor, @PathVariable parcelId: UUID): ParcelDetailResponse =
        ParcelDetailResponse.from(queries.detail(actor, ParcelId(parcelId)))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun register(actor: BackOfficeActor, @Valid @RequestBody request: RegisterParcelRequest): ParcelDetailResponse {
        val command = RegisterParcel(
            trackingNumber = TrackingNumber.of(request.trackingNumber),
            carrierCode = CarrierCode(request.carrierCode),
            orderReference = OrderReference(request.orderReference),
            deliveryInstructions = request.deliveryInstructions,
        )
        return ParcelDetailResponse.from(registerParcel.register(actor, command))
    }
}
