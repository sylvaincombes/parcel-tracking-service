package com.shoplane.parceltracking.domain.carrier

interface CarrierDirectory {
    fun findCarrier(code: CarrierCode): Carrier?

    fun activeCarriers(): List<Carrier>
}

class UnknownCarrierException(code: CarrierCode) : RuntimeException("Carrier $code is not known by the carrier registry")

class RetiredCarrierException(code: CarrierCode) : RuntimeException("Carrier $code is retired and no longer accepts parcels")
