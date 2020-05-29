package org.coepi.api.server

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.AutoHeadResponse
import io.ktor.features.CachingHeaders
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.CacheControl
import io.ktor.http.content.CachingOptions
import io.ktor.http.content.caching
import io.ktor.response.respondText
import io.ktor.routing.*
import org.slf4j.event.Level

fun Application.main() {
    install(AutoHeadResponse)

    install(CachingHeaders)

    install(CallLogging) {
        level = Level.INFO
    }

    install(DefaultHeaders)

    routing {
        tcnReports()
    }
}
