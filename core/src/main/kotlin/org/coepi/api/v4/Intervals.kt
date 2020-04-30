package org.coepi.api.v4

import java.time.Instant

object Intervals {
    const val INTERVAL_LENGTH_MS: Long = 6 * 3600 * 1000
}

fun generateIntervalForTimestamp(timestamp: Long): Long =
    timestamp / Intervals.INTERVAL_LENGTH_MS

fun generateIntervalForCurrentTime(): Long =
    generateIntervalForTimestamp(Instant.now().toEpochMilli())
