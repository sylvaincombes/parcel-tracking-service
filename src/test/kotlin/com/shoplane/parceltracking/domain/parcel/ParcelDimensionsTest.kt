package com.shoplane.parceltracking.domain.parcel

import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test

class ParcelDimensionsTest {
    @Test
    fun `rejects a parcel without weight`() {
        assertThatIllegalArgumentException().isThrownBy {
            ParcelDimensions(weightGrams = 0, lengthMm = 400, widthMm = 300, heightMm = 125)
        }
    }

    @Test
    fun `rejects a negative side`() {
        assertThatIllegalArgumentException().isThrownBy {
            ParcelDimensions(weightGrams = 2350, lengthMm = 400, widthMm = -300, heightMm = 125)
        }
    }
}
