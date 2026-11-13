package com.shoplane.parceltracking.domain.parcel

import java.net.URI

class PublicTrackingPage(baseUrl: String) {
    private val baseUrl = baseUrl.trimEnd('/')

    fun linkFor(trackingNumber: TrackingNumber): URI = URI.create("$baseUrl/${trackingNumber.value}")
}
