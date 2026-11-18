package com.shoplane.parceltracking.domain.parcel

class ParcelNotFoundException(reference: String) : RuntimeException("Parcel $reference not found")

class IllegalStatusTransitionException(
    parcelId: ParcelId,
    from: ParcelStatus,
    to: ParcelStatus,
) : RuntimeException("Parcel $parcelId cannot move from $from to $to")

class ParcelNotLateException(parcelId: ParcelId) : RuntimeException("Parcel $parcelId has not been flagged late")

class DuplicateTrackingNumberException(
    trackingNumber: TrackingNumber,
) : RuntimeException("A parcel with tracking number $trackingNumber is already registered")
