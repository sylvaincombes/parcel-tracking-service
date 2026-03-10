package com.shoplane.parceltracking.presentation.webhook

import com.shoplane.parceltracking.domain.parcel.TrackingEventType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CarrierEventCodeMapperTest {
    @Test
    fun `maps normalized carrier codes regardless of case`() {
        assertThat(CarrierEventCodeMapper.typeOf("dlv")).isEqualTo(TrackingEventType.DELIVERED)
        assertThat(CarrierEventCodeMapper.typeOf(" OFD ")).isEqualTo(TrackingEventType.OUT_FOR_DELIVERY)
        assertThat(CarrierEventCodeMapper.typeOf("ATT")).isEqualTo(TrackingEventType.DELIVERY_ATTEMPT_FAILED)
    }

    @Test
    fun `codes outside the normalized set are not mapped`() {
        assertThat(CarrierEventCodeMapper.typeOf("CUSTOMS_HOLD")).isNull()
    }
}
