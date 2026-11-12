package com.shoplane.parceltracking.application

import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.domain.parcel.Parcel
import com.shoplane.parceltracking.domain.parcel.ParcelRepository
import com.shoplane.parceltracking.domain.parcel.SellerId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class ExportPeriod(val from: LocalDate, val to: LocalDate) {
    init {
        require(!to.isBefore(from)) { "Export period ends on $to, before it starts on $from" }
        require(ChronoUnit.DAYS.between(from, to) < MAX_DAYS) { "An export covers at most $MAX_DAYS days" }
    }

    val startsAt: Instant
        get() = from.atStartOfDay(SELLER_TIME_ZONE).toInstant()

    val endsBefore: Instant
        get() = to.plusDays(1).atStartOfDay(SELLER_TIME_ZONE).toInstant()

    companion object {
        const val MAX_DAYS = 92
        val SELLER_TIME_ZONE: ZoneId = ZoneId.of("Europe/Paris")
    }
}

@Service
class ExportParcelsUseCase(
    private val parcels: ParcelRepository,
) {
    @Transactional(readOnly = true)
    fun export(actor: BackOfficeActor, sellerId: SellerId, period: ExportPeriod): List<Parcel> {
        val exported = parcels.findBySellerRegisteredBetween(sellerId, period.startsAt, period.endsBefore)
        logger.info(
            "User {} exported {} parcels of seller {} registered from {} to {}",
            actor.userId,
            exported.size,
            sellerId,
            period.from,
            period.to,
        )
        return exported
    }

    private companion object {
        val logger = LoggerFactory.getLogger(ExportParcelsUseCase::class.java)
    }
}
