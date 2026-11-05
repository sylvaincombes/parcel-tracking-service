package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.parcel.OrderReference
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateOrderReferenceUseCase(
    private val parcels: ParcelRepository,
    private val accessPolicy: ParcelAccessPolicy,
) {
    @Transactional
    fun update(actor: BackOfficeActor, parcelId: ParcelId, orderReference: OrderReference): Parcel {
        val parcel = parcels.findById(parcelId) ?: throw ParcelNotFoundException(parcelId.toString())
        accessPolicy.requireCanManage(actor, parcel)
        parcel.changeOrderReference(orderReference)
        return parcels.save(parcel)
    }
}
