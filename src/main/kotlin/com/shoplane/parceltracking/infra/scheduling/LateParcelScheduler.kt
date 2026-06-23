package com.shoplane.parceltracking.infra.scheduling

import com.shoplane.parceltracking.application.DetectLateParcelsUseCase
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "parcel-tracking.late-detection", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class LateParcelScheduler(
    private val detectLateParcels: DetectLateParcelsUseCase,
) {
    @Scheduled(cron = "\${parcel-tracking.late-detection.cron}", zone = "UTC")
    fun detectLateParcels() {
        detectLateParcels.detect()
    }
}
