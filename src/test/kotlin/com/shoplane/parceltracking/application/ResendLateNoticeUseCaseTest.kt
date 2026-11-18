package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.application.access.ParcelAccessPolicy
import com.shoplane.parceltracking.domain.calendar.BusinessCalendar
import com.shoplane.parceltracking.domain.carrier.TransitDays
import com.shoplane.parceltracking.domain.event.ParcelLateDetected
import com.shoplane.parceltracking.domain.lateness.LateParcelDetector
import com.shoplane.parceltracking.domain.parcel.ParcelNotLateException
import com.shoplane.parceltracking.fixtures.InMemoryParcelRepository
import com.shoplane.parceltracking.fixtures.LIGHTING_SELLER
import com.shoplane.parceltracking.fixtures.RecordingParcelEventPublisher
import com.shoplane.parceltracking.fixtures.StubCarrierDirectory
import com.shoplane.parceltracking.fixtures.aCarrier
import com.shoplane.parceltracking.fixtures.aPickedUpParcel
import com.shoplane.parceltracking.fixtures.aSellerAdmin
import com.shoplane.parceltracking.fixtures.aSupportAgent
import com.shoplane.parceltracking.fixtures.fixedClockAt
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class ResendLateNoticeUseCaseTest {
    private val lateParcel = aPickedUpParcel(trackingNumber = "CW4400112233FR", pickedUpAt = "2026-11-02T14:00:00Z")
        .apply { markLate(Instant.parse("2026-11-06T09:15:00Z")) }
    private val parcelOnTime = aPickedUpParcel(trackingNumber = "CW4400998877FR", pickedUpAt = "2026-11-17T14:00:00Z")
    private val publisher = RecordingParcelEventPublisher()
    private val useCase = ResendLateNoticeUseCase(
        parcels = InMemoryParcelRepository(lateParcel, parcelOnTime),
        carriers = StubCarrierDirectory(aCarrier(transitDays = TransitDays(min = 1, max = 2))),
        accessPolicy = ParcelAccessPolicy(),
        detector = LateParcelDetector(BusinessCalendar(emptySet()), graceBusinessDays = 1),
        publisher = publisher,
        clock = fixedClockAt("2026-11-18T10:00:00Z"),
    )

    @Test
    fun `a support agent resends the late notice with the parcel's delivery deadline`() {
        useCase.resend(aSupportAgent(), lateParcel.id)

        assertThat(publisher.published.single()).isInstanceOfSatisfying(ParcelLateDetected::class.java) {
            assertThat(it.parcelId).isEqualTo(lateParcel.id)
            assertThat(it.sellerId).isEqualTo(lateParcel.sellerId)
            assertThat(it.deliveryDeadline).isEqualTo(LocalDate.parse("2026-11-05"))
        }
    }

    @Test
    fun `a seller cannot resend the late notice of its own parcel`() {
        assertThatThrownBy { useCase.resend(aSellerAdmin(), lateParcel.id) }
            .isInstanceOf(AccessDeniedException::class.java)
        assertThat(publisher.published).isEmpty()
    }

    @Test
    fun `another seller cannot resend the late notice`() {
        assertThatThrownBy { useCase.resend(aSellerAdmin(LIGHTING_SELLER), lateParcel.id) }
            .isInstanceOf(AccessDeniedException::class.java)
        assertThat(publisher.published).isEmpty()
    }

    @Test
    fun `a parcel that was never flagged late has no notice to resend`() {
        assertThatThrownBy { useCase.resend(aSupportAgent(), parcelOnTime.id) }
            .isInstanceOf(ParcelNotLateException::class.java)
        assertThat(publisher.published).isEmpty()
    }
}
