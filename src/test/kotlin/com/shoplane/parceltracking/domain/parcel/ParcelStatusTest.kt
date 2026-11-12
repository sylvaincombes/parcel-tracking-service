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
    fun `a parcel in transit, out for delivery or after a failed attempt can wait at a pickup point`() {
        assertThat(ParcelStatus.entries.filter { it.canMoveTo(ParcelStatus.AWAITING_COLLECTION) })
            .containsExactlyInAnyOrder(ParcelStatus.IN_TRANSIT, ParcelStatus.OUT_FOR_DELIVERY, ParcelStatus.DELIVERY_FAILED)
    }

    @Test
    fun `a parcel waiting at a pickup point is either collected or returned to the seller`() {
        assertThat(ParcelStatus.AWAITING_COLLECTION.isTerminal).isFalse()
        assertThat(ParcelStatus.entries.filter { ParcelStatus.AWAITING_COLLECTION.canMoveTo(it) })
            .containsExactlyInAnyOrder(ParcelStatus.DELIVERED, ParcelStatus.RETURNED_TO_SENDER)
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
