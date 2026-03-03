package com.shoplane.parceltracking.fixtures

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

fun fixedClockAt(instant: String): Clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC)
