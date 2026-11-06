package com.shoplane.parceltracking.presentation.backoffice

import com.shoplane.parceltracking.fixtures.aPickedUpParcel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class ParcelSummaryResponseTest {
    @Test
    fun `a late parcel shows since when it is late`() {
        val detectedAt = Instant.parse("2026-11-06T09:15:00Z")
        val parcel = aPickedUpParcel().apply { markLate(detectedAt) }

        assertThat(ParcelSummaryResponse.from(parcel).lateSince).isEqualTo(detectedAt)
    }

    @Test
    fun `a parcel on time has no late date`() {
        assertThat(ParcelSummaryResponse.from(aPickedUpParcel()).lateSince).isNull()
    }
}
