package org.coepi.api.common

import java.util.*

fun <A : Any> Optional<A>.orNull(): A? = this.orElse(null)
