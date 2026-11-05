package com.shoplane.parceltracking.domain.parcel

import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ParcelDimensionsTest {
    @Test
    fun `rejects a parcel without weight`() {
        assertThatIllegalArgumentException().isThrownBy {
            ParcelDimensions(BigDecimal.ZERO, BigDecimal("40.0"), BigDecimal("30.0"), BigDecimal("12.5"))
        }
    }

    @Test
    fun `rejects a negative side`() {
        assertThatIllegalArgumentException().isThrownBy {
            ParcelDimensions(BigDecimal("2.350"), BigDecimal("40.0"), BigDecimal("-30.0"), BigDecimal("12.5"))
        }
    }
}
