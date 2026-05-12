package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.fixtures.GARDEN_TOOLS_SELLER
import com.shoplane.parceltracking.fixtures.LIGHTING_SELLER
import com.shoplane.parceltracking.fixtures.aRegisteredParcel
import com.shoplane.parceltracking.fixtures.aSellerAdmin
import com.shoplane.parceltracking.fixtures.aSellerOperator
import com.shoplane.parceltracking.fixtures.aSellerViewer
import com.shoplane.parceltracking.fixtures.aSupportAgent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ParcelAccessPolicyTest {
    private val policy = ParcelAccessPolicy()
    private val parcel = aRegisteredParcel(sellerId = GARDEN_TOOLS_SELLER)

    @Test
    fun `any user of the owning seller can view the parcel`() {
        assertThatCode { policy.requireCanView(aSellerViewer(), parcel) }.doesNotThrowAnyException()
    }

    @Test
    fun `a user of another seller cannot view the parcel`() {
        assertThatThrownBy { policy.requireCanView(aSellerAdmin(LIGHTING_SELLER), parcel) }
            .isInstanceOf(AccessDeniedException::class.java)
    }

    @Test
    fun `operators of the owning seller can manage the parcel`() {
        assertThatCode { policy.requireCanManage(aSellerOperator(), parcel) }.doesNotThrowAnyException()
    }

    @Test
    fun `read-only users of the owning seller cannot manage the parcel`() {
        assertThatThrownBy { policy.requireCanManage(aSellerViewer(), parcel) }
            .isInstanceOf(AccessDeniedException::class.java)
    }

    @Test
    fun `support agents can view and manage any parcel`() {
        assertThatCode {
            policy.requireCanView(aSupportAgent(), parcel)
            policy.requireCanManage(aSupportAgent(), parcel)
        }.doesNotThrowAnyException()
    }

    @Test
    fun `sellers are scoped to their own parcels when listing`() {
        assertThat(policy.resolveSellerScope(aSellerViewer(), null)).isEqualTo(GARDEN_TOOLS_SELLER)
        assertThatThrownBy { policy.resolveSellerScope(aSellerViewer(), LIGHTING_SELLER) }
            .isInstanceOf(AccessDeniedException::class.java)
    }

    @Test
    fun `support agents must name the seller they list parcels for`() {
        assertThat(policy.resolveSellerScope(aSupportAgent(), LIGHTING_SELLER)).isEqualTo(LIGHTING_SELLER)
        assertThatIllegalArgumentException().isThrownBy { policy.resolveSellerScope(aSupportAgent(), null) }
    }
}
