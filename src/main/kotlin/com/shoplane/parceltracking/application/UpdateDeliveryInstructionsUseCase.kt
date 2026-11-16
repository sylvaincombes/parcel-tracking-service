package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelId
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateDeliveryInstructionsUseCase(
    private val parcels: ParcelRepository,
) {
    @Transactional
    fun update(actor: BackOfficeActor, parcelId: ParcelId, instructions: String?): Parcel {
        val parcel = parcels.findById(parcelId) ?: throw ParcelNotFoundException(parcelId.toString())
        parcel.changeDeliveryInstructions(instructions)
        val saved = parcels.save(parcel)
        logger.info("User {} changed the delivery instructions of parcel {}", actor.userId, parcelId)
        return saved
    }

    private companion object {
        val logger = LoggerFactory.getLogger(UpdateDeliveryInstructionsUseCase::class.java)
    }
}
