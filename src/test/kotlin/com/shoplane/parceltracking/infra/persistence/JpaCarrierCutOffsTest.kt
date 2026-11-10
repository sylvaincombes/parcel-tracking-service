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
    private val jpa = mockk<CarrierCutOffJpaRepository>()
    private val cutOffs = JpaCarrierCutOffs(jpa)

    @Test
    fun `returns the pick-up cut-off stored for the carrier`() {
        every { jpa.findById("colisway") } returns Optional.of(CarrierCutOffEntity("colisway", LocalTime.of(17, 0)))

        assertThat(cutOffs.cutOffFor(COLISWAY, LocalDate.parse("2026-11-16"))).isEqualTo(LocalTime.of(17, 0))
    }

    @Test
    fun `a carrier without a stored cut-off has none`() {
        every { jpa.findById("parcelwave") } returns Optional.empty()

        assertThat(cutOffs.cutOffFor(PARCELWAVE, LocalDate.parse("2026-11-16"))).isNull()
    }
}
