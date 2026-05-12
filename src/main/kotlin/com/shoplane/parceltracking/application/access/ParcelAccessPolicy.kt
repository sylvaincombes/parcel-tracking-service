package com.shoplane.parceltracking.application.access

import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.SellerId
import org.springframework.stereotype.Component

@Component
class ParcelAccessPolicy {
    fun requireCanView(actor: BackOfficeActor, parcel: Parcel) {
        if (actor.isSupportAgent || actor.sellerId == parcel.sellerId) {
            return
        }
        throw AccessDeniedException("User ${actor.userId} cannot view parcel ${parcel.id}")
    }

    fun requireCanManage(actor: BackOfficeActor, parcel: Parcel) {
        if (actor.isSupportAgent || (actor.canManageSellerParcels && actor.sellerId == parcel.sellerId)) {
            return
        }
        throw AccessDeniedException("User ${actor.userId} cannot manage parcel ${parcel.id}")
    }

    fun resolveSellerScope(actor: BackOfficeActor, requestedSellerId: SellerId?): SellerId =
        when {
            actor.isSupportAgent ->
                requestedSellerId ?: throw IllegalArgumentException("Support agents must provide a sellerId")
            actor.sellerId != null && (requestedSellerId == null || requestedSellerId == actor.sellerId) ->
                actor.sellerId
            else ->
                throw AccessDeniedException("User ${actor.userId} cannot list parcels of seller $requestedSellerId")
        }
}
