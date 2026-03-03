package com.shoplane.parceltracking.domain.parcel

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ParcelStatusTest {
    @ParameterizedTest
    @EnumSource(value = ParcelStatus::class, names = ["DELIVERED", "RETURNED_TO_SENDER", "CANCELLED"])
    fun `terminal statuses accept no further transition`(status: ParcelStatus) {
        assertThat(status.isTerminal).isTrue()
        assertThat(ParcelStatus.entries.filter { status.canMoveTo(it) }).isEmpty()
    }

    @Test
    fun `a parcel out for delivery can be delivered or fail delivery`() {
        assertThat(ParcelStatus.OUT_FOR_DELIVERY.canMoveTo(ParcelStatus.DELIVERED)).isTrue()
        assertThat(ParcelStatus.OUT_FOR_DELIVERY.canMoveTo(ParcelStatus.DELIVERY_FAILED)).isTrue()
    }

    @Test
    fun `a registered parcel cannot be delivered before being handed to the carrier`() {
        assertThat(ParcelStatus.REGISTERED.canMoveTo(ParcelStatus.DELIVERED)).isFalse()
    }

    @Test
    fun `only a registered parcel can be cancelled`() {
        assertThat(ParcelStatus.entries.filter { it.canMoveTo(ParcelStatus.CANCELLED) })
            .containsExactly(ParcelStatus.REGISTERED)
    }
}
