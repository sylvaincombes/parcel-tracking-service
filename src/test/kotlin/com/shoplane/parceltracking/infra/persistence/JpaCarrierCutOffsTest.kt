package com.shoplane.parceltracking.infra.persistence

import com.shoplane.parceltracking.fixtures.COLISWAY
import com.shoplane.parceltracking.fixtures.PARCELWAVE
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.Optional

class JpaCarrierCutOffsTest {
    private val jpa = mockk<CarrierCutOffJpaRepository> {
        every { findById(any()) } returns Optional.empty()
    }
    private val cutOffs = JpaCarrierCutOffs(jpa)

    @Test
    fun `returns the pick-up cut-off of the requested weekday`() {
        storeCutOff("colisway", weekday = 1, LocalTime.of(17, 0))
        storeCutOff("colisway", weekday = 5, LocalTime.of(14, 30))

        assertThat(cutOffs.cutOffFor(COLISWAY, LocalDate.parse("2026-11-16"))).isEqualTo(LocalTime.of(17, 0))
        assertThat(cutOffs.cutOffFor(COLISWAY, LocalDate.parse("2026-11-20"))).isEqualTo(LocalTime.of(14, 30))
    }

    @Test
    fun `a weekday without a stored cut-off has none`() {
        storeCutOff("colisway", weekday = 1, LocalTime.of(17, 0))

        assertThat(cutOffs.cutOffFor(COLISWAY, LocalDate.parse("2026-11-17"))).isNull()
    }

    @Test
    fun `a carrier without a stored cut-off has none`() {
        assertThat(cutOffs.cutOffFor(PARCELWAVE, LocalDate.parse("2026-11-16"))).isNull()
    }

    private fun storeCutOff(carrierCode: String, weekday: Short, cutOffTime: LocalTime) {
        val key = CarrierCutOffKey(carrierCode, weekday)
        every { jpa.findById(key) } returns Optional.of(CarrierCutOffEntity(key, cutOffTime))
    }
}
