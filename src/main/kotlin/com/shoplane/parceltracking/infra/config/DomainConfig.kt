package com.shoplane.parceltracking.infra.config

import com.shoplane.parceltracking.domain.attempt.DeliveryAttemptClassifier
import com.shoplane.parceltracking.domain.calendar.BusinessCalendar
import com.shoplane.parceltracking.domain.carrier.CarrierCode
import com.shoplane.parceltracking.domain.eta.EtaCalculator
import com.shoplane.parceltracking.domain.lateness.LateParcelDetector
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.LocalDate

@ConfigurationProperties(prefix = "parcel-tracking.calendar")
data class CalendarProperties(
    val publicHolidays: Set<LocalDate> = emptySet(),
)

@ConfigurationProperties(prefix = "parcel-tracking.late-detection")
data class LateDetectionProperties(
    val graceBusinessDays: Int = 1,
    val excludedCarrierCodes: Set<String> = emptySet(),
)

@Configuration
@EnableConfigurationProperties(CalendarProperties::class, LateDetectionProperties::class)
class DomainConfig {
    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun businessCalendar(properties: CalendarProperties) = BusinessCalendar(properties.publicHolidays)

    @Bean
    fun etaCalculator(calendar: BusinessCalendar) = EtaCalculator(calendar)

    @Bean
    fun lateParcelDetector(calendar: BusinessCalendar, properties: LateDetectionProperties) =
        LateParcelDetector(
            calendar = calendar,
            graceBusinessDays = properties.graceBusinessDays,
            excludedCarriers = properties.excludedCarrierCodes.map(::CarrierCode).toSet(),
        )

    @Bean
    fun deliveryAttemptClassifier() = DeliveryAttemptClassifier()
}
