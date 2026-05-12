package com.shoplane.parceltracking.fixtures

import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.application.access.BackOfficeRole
import com.shoplane.parceltracking.domain.parcel.SellerId

fun aSellerAdmin(sellerId: SellerId = GARDEN_TOOLS_SELLER) =
    BackOfficeActor(userId = "seller-admin-${sellerId.value}", sellerId = sellerId, roles = setOf(BackOfficeRole.SELLER_ADMIN))

fun aSellerOperator(sellerId: SellerId = GARDEN_TOOLS_SELLER) =
    BackOfficeActor(userId = "seller-operator-${sellerId.value}", sellerId = sellerId, roles = setOf(BackOfficeRole.SELLER_OPERATOR))

fun aSellerViewer(sellerId: SellerId = GARDEN_TOOLS_SELLER) =
    BackOfficeActor(userId = "seller-viewer-${sellerId.value}", sellerId = sellerId, roles = emptySet())

fun aSupportAgent() =
    BackOfficeActor(userId = "support-agent-17", sellerId = null, roles = setOf(BackOfficeRole.SUPPORT_AGENT))
