package com.shoplane.parceltracking.fixtures

import com.shoplane.parceltracking.domain.event.ParcelDomainEvent
import com.shoplane.parceltracking.domain.event.ParcelEventPublisher

class RecordingParcelEventPublisher : ParcelEventPublisher {
    val published = mutableListOf<ParcelDomainEvent>()

    override fun publish(event: ParcelDomainEvent) {
        published += event
    }
}
