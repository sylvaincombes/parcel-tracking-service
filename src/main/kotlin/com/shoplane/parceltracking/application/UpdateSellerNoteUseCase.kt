package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateSellerNoteUseCase(
    private val parcels: ParcelRepository,
    private val accessPolicy: ParcelAccessPolicy,
) {
    @Transactional
    fun update(actor: BackOfficeActor, parcelId: ParcelId, note: String?): Parcel {
        val parcel = parcels.findById(parcelId) ?: throw ParcelNotFoundException(parcelId.toString())
        accessPolicy.requireCanManage(actor, parcel)
        parcel.changeSellerNote(note)
        return parcels.save(parcel)
    }
}
