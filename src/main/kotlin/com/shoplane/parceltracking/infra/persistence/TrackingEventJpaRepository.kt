package com.shoplane.parceltracking.infra.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface TrackingEventJpaRepository : JpaRepository<TrackingEventEntity, Long>
