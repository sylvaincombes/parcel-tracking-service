package com.shoplane.parceltracking.presentation.backoffice

import com.shoplane.parceltracking.domain.parcel.TrackingEventType
import com.shoplane.parceltracking.fixtures.aPickedUpParcel
import com.shoplane.parceltracking.fixtures.aRegisteredParcel
import com.shoplane.parceltracking.fixtures.event
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class ParcelCsvTest {
    @Test
    fun `lists one parcel per line after the header`() {
        val delivered = aPickedUpParcel(trackingNumber = "CW4400112233FR").apply {
            record(event(TrackingEventType.OUT_FOR_DELIVERY, "2026-11-04T06:10:00Z"), Instant.parse("2026-11-04T06:10:00Z"))
            record(event(TrackingEventType.DELIVERED, "2026-11-04T11:42:00Z"), Instant.parse("2026-11-04T11:42:00Z"))
        }
        val registered = aRegisteredParcel(trackingNumber = "CW4400998877FR", orderReference = "SO-2026-118790")

        val csv = ParcelCsv.write(listOf(delivered, registered))

        assertThat(csv).isEqualTo(
            "tracking_number,carrier,order_reference,status,registered_at,delivered_at\r\n" +
                "CW4400112233FR,colisway,SO-2026-118734,DELIVERED,2026-11-02T08:30:00Z,2026-11-04T11:42:00Z\r\n" +
                "CW4400998877FR,colisway,SO-2026-118790,REGISTERED,2026-11-02T08:30:00Z,\r\n",
        )
    }

    @Test
    fun `an export without parcels only has the header`() {
        assertThat(ParcelCsv.write(emptyList()))
            .isEqualTo("tracking_number,carrier,order_reference,status,registered_at,delivered_at\r\n")
    }

    @Test
    fun `order references with commas or quotes are quoted`() {
        val parcel = aRegisteredParcel(orderReference = "Order 118734, \"garden\" set")

        assertThat(ParcelCsv.write(listOf(parcel)).lines()[1]).contains(",\"Order 118734, \"\"garden\"\" set\",")
    }

    @Test
    fun `order references starting like a spreadsheet formula are kept as text`() {
        val parcel = aRegisteredParcel(orderReference = "=HYPERLINK(\"https://example.com\")")

        assertThat(ParcelCsv.write(listOf(parcel)).lines()[1]).contains(",\"'=HYPERLINK(\"\"https://example.com\"\")\",")
    }
}
