package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.domain.calendar.BusinessCalendar
import com.shoplane.parceltracking.domain.carrier.TransitDays
import com.shoplane.parceltracking.domain.event.ParcelLateDetected
import com.shoplane.parceltracking.domain.lateness.LateParcelDetector
import com.shoplane.parceltracking.fixtures.InMemoryParcelRepository
import com.shoplane.parceltracking.fixtures.PARCELWAVE
import com.shoplane.parceltracking.fixtures.RecordingParcelEventPublisher
import com.shoplane.parceltracking.fixtures.StubCarrierDirectory
import com.shoplane.parceltracking.fixtures.aCarrier
import com.shoplane.parceltracking.fixtures.aPickedUpParcel
import com.shoplane.parceltracking.fixtures.fixedClockAt
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate

class DetectLateParcelsUseCaseTest {
    private val stuckParcel = aPickedUpParcel(trackingNumber = "CW4400112233FR", pickedUpAt = "2026-11-02T14:00:00Z")
    private val freshParcel = aPickedUpParcel(trackingNumber = "CW4400998877FR", pickedUpAt = "2026-11-18T14:00:00Z")
    private val parcels = InMemoryParcelRepository(stuckParcel, freshParcel)
    private val publisher = RecordingParcelEventPublisher()
    private val transactionTemplate = mockk<TransactionTemplate> {
        every { execute(any<TransactionCallback<Boolean>>()) } answers {
            firstArg<TransactionCallback<Boolean>>().doInTransaction(mockk(relaxed = true))
        }
    }
    private val useCase = DetectLateParcelsUseCase(
        parcels = parcels,
        carriers = StubCarrierDirectory(aCarrier(transitDays = TransitDays(min = 1, max = 2))),
        detector = LateParcelDetector(BusinessCalendar(emptySet()), graceBusinessDays = 1),
        publisher = publisher,
        transactionTemplate = transactionTemplate,
        clock = fixedClockAt("2026-11-19T15:00:00Z"),
    )

    @Test
    fun `flags parcels past their delivery deadline and notifies downstream`() {
        val flagged = useCase.detect(batchSize = 1)

        assertThat(flagged).isEqualTo(1)
        assertThat(parcels.findById(stuckParcel.id)?.isLate).isTrue()
        assertThat(parcels.findById(freshParcel.id)?.isLate).isFalse()
        assertThat(publisher.published.single()).isInstanceOfSatisfying(ParcelLateDetected::class.java) {
            assertThat(it.parcelId).isEqualTo(stuckParcel.id)
            assertThat(it.deliveryDeadline).isBefore(LocalDate.parse("2026-11-19"))
        }
    }

    @Test
    fun `a second run does not flag the same parcel again`() {
        useCase.detect()
        publisher.published.clear()

        assertThat(useCase.detect()).isZero()
        assertThat(publisher.published).isEmpty()
    }

    @Test
    fun `a parcel whose carrier is missing from the registry is skipped without stopping the run`() {
        val stuckWithMissingCarrier = aPickedUpParcel(
            trackingNumber = "PW7781200456FR",
            carrierCode = PARCELWAVE,
            pickedUpAt = "2026-11-02T14:00:00Z",
        )
        val parcels = InMemoryParcelRepository(stuckWithMissingCarrier, stuckParcel)
        val useCase = DetectLateParcelsUseCase(
            parcels = parcels,
            carriers = StubCarrierDirectory(aCarrier(transitDays = TransitDays(min = 1, max = 2))),
            detector = LateParcelDetector(BusinessCalendar(emptySet()), graceBusinessDays = 1),
            publisher = publisher,
            transactionTemplate = transactionTemplate,
            clock = fixedClockAt("2026-11-19T15:00:00Z"),
        )

        val flagged = useCase.detect(batchSize = 1)

        assertThat(flagged).isEqualTo(1)
        assertThat(parcels.findById(stuckWithMissingCarrier.id)?.isLate).isFalse()
        assertThat(parcels.findById(stuckParcel.id)?.isLate).isTrue()
    }
}
