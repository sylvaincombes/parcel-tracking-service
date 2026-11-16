package com.shoplane.parceltracking.domain.parcel

class ParcelNotFoundException(reference: String) : RuntimeException("Parcel $reference not found")

class IllegalStatusTransitionException(
    parcelId: ParcelId,
    from: ParcelStatus,
    to: ParcelStatus,
) : RuntimeException("Parcel $parcelId cannot move from $from to $to")

class DeliveryInstructionsLockedException(
    parcelId: ParcelId,
    status: ParcelStatus,
) : RuntimeException("Delivery instructions of parcel $parcelId cannot change while it is $status")

class DuplicateTrackingNumberException(
    trackingNumber: TrackingNumber,
) : RuntimeException("A parcel with tracking number $trackingNumber is already registered")
