package com.shoplane.parceltracking.presentation.publicapi

import com.shoplane.parceltracking.application.GetPublicTrackingUseCase
import com.shoplane.parceltracking.domain.parcel.TrackingNumber
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/public/v1/tracking")
class PublicTrackingController(
    private val getPublicTracking: GetPublicTrackingUseCase,
) {
    @GetMapping("/{trackingNumber}")
    fun track(@PathVariable trackingNumber: String): PublicTrackingResponse =
        PublicTrackingResponse.from(getPublicTracking.track(TrackingNumber.of(trackingNumber)))
}
