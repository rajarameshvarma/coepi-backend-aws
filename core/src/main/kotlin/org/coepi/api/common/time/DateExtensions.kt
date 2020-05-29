package org.coepi.api.common.time

import java.time.*

private val UTC = ZoneId.of("UTC")

fun Instant.toUtcLocalDate() = LocalDate.ofInstant(this, UTC)
