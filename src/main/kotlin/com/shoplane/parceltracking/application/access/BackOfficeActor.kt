package com.shoplane.parceltracking.application.access

import com.shoplane.parceltracking.domain.parcel.SellerId

enum class BackOfficeRole {
    SELLER_ADMIN,
    SELLER_OPERATOR,
    SUPPORT_AGENT,
}

data class BackOfficeActor(
    val userId: String,
    val sellerId: SellerId?,
    val roles: Set<BackOfficeRole>,
) {
    val isSupportAgent: Boolean
        get() = BackOfficeRole.SUPPORT_AGENT in roles

    val canManageSellerParcels: Boolean
        get() = sellerId != null && (BackOfficeRole.SELLER_ADMIN in roles || BackOfficeRole.SELLER_OPERATOR in roles)
}

class AccessDeniedException(message: String) : RuntimeException(message)
