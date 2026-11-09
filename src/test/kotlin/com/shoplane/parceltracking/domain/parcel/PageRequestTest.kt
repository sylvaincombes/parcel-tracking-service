package com.shoplane.parceltracking.domain.parcel

import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test

class PageRequestTest {
    @Test
    fun `a page holds at most 50 parcels`() {
        assertThatCode { PageRequest(page = 0, size = 50) }.doesNotThrowAnyException()
        assertThatIllegalArgumentException().isThrownBy { PageRequest(page = 0, size = 51) }
    }

    @Test
    fun `a page holds at least one parcel`() {
        assertThatIllegalArgumentException().isThrownBy { PageRequest(page = 0, size = 0) }
    }
}
