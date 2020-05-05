package org.coepi.api.v4.http

import com.fasterxml.jackson.databind.ObjectMapper
import org.coepi.api.InvalidTCNSignatureException
import org.coepi.api.TCNClientException
import org.coepi.api.UnexpectedIntervalLengthException
import org.coepi.api.common.base64Decode
import org.coepi.api.common.orNull
import org.coepi.api.common.toByteBuffer
import org.coepi.api.v4.Intervals
import org.coepi.api.v4.reports.TCNReportService
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.*

class TCNHttpHandler(
    private val objectMapper: ObjectMapper,
    private val reportService: TCNReportService
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun getReport(parameters: Map<String, String>): HttpResponse {
        return try {
            val (maybeDate, maybeInterval) = parseQueryParameters(parameters)

            logger.info("Querying reports with date: $maybeDate and intervalNumber: $maybeInterval")

            val reports =
                reportService
                    .getReports(maybeDate.orNull(), maybeInterval.orNull())
                    .map { it.report }

            logger.info("Number of reports retrieved successfully: ${reports.size}")

            Ok(objectMapper.writeValueAsBytes(reports).toByteBuffer())
        } catch (ex: TCNClientException) {
            logger.info("Failed to retrieve report due to client error", ex)

            BadRequest(ex.message.orEmpty())
        } catch (ex: UnexpectedIntervalLengthException) {
            logger.info("Failed to retrieve report due to bad intervalLength used by client", ex)
            Unauthorized(ex.message.orEmpty())
        }
    }

    fun postReport(body: ByteBuffer): HttpResponse =
        try {
            val reportData = body.base64Decode()
            val savedReport = reportService.saveReport(reportData)
            logger.info("Successfully added report ${savedReport.reportId}")

            Ok()
        } catch (ex: InvalidTCNSignatureException) {
            logger.info("Failed to put report due to illegal TCN Signature", ex)
            Unauthorized(ex.message.orEmpty())
        } catch (ex: IllegalArgumentException) {
            logger.info("Failed to put report due to client error", ex)
            BadRequest(ex.message.orEmpty())
        }
}

const val DATE_KEY = "date"
const val INTERVAL_NUMBER_KEY = "intervalNumber"
const val INTERVAL_LENGTH_MS_KEY = "intervalLengthMs"

private fun parseQueryParameters(
    parameters: Map<String, String>
): Pair<Optional<LocalDate>, Optional<Long>> {
    var date = Optional.empty<LocalDate>()
    var intervalNumber = Optional.empty<Long>()

    try {
        if (!parameters[DATE_KEY].isNullOrEmpty()) {
            val rawDate = LocalDate.parse(parameters[DATE_KEY]) // Unit Test
            date = Optional.of(rawDate)
        }

        if (parameters.containsKey(INTERVAL_NUMBER_KEY)) {
            val rawBatch = parameters[INTERVAL_NUMBER_KEY]?.toLong()
            intervalNumber = if (rawBatch != null) Optional.of(rawBatch) else Optional.empty()

            if (!parameters.containsKey(INTERVAL_LENGTH_MS_KEY)) {
                throw TCNClientException(
                    "$INTERVAL_LENGTH_MS_KEY query parameter is required if " +
                            " $INTERVAL_LENGTH_MS_KEY is provided"
                )
            }
            val intervalLengthMs = parameters[INTERVAL_LENGTH_MS_KEY]?.toLong()

            if (intervalLengthMs != Intervals.INTERVAL_LENGTH_MS) {
                throw UnexpectedIntervalLengthException(
                    "$intervalLengthMs is invalid for the date " +
                            "$DATE_KEY. Please use ${Intervals.INTERVAL_LENGTH_MS} to calculate $INTERVAL_NUMBER_KEY"
                )
            }
        }
    } catch (ex: DateTimeParseException) {
        throw TCNClientException("$DATE_KEY in illegal date format.", ex)
    } catch (ex: NumberFormatException) {
        throw TCNClientException(
            "$INTERVAL_NUMBER_KEY or $INTERVAL_LENGTH_MS_KEY in " +
                    "illegal number format.", ex
        )
    }

    if (intervalNumber.isPresent && intervalNumber.get() < 0) {
        throw TCNClientException("$INTERVAL_LENGTH_MS_KEY should be positive")
    }

    return Pair(date, intervalNumber)
}
