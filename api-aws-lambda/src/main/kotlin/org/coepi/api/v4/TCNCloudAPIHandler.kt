package org.coepi.api.v4

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.*
import org.coepi.api.InvalidTCNSignatureException
import org.coepi.api.TCNClientException
import org.coepi.api.UnexpectedIntervalLengthException
import org.coepi.api.common.orNull
import org.coepi.api.v4.dao.TCNReportsDao
import org.coepi.api.v4.reports.TCNReportService
import org.coepi.api.v4.reports.TCNReportServiceImpl
import org.slf4j.LoggerFactory

class TCNCloudAPIHandler(
    private val reportService: TCNReportService,
    private val objectMapper: ObjectMapper
) : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private val logger = LoggerFactory.getLogger(TCNCloudAPIHandler::class.java)

    /**
     * Zero-arg constructor to initialize the class in Lambda.
     */
    constructor() : this(
        reportService = TCNReportServiceImpl(
            clock = Clock.systemUTC(),
            reportsDao = TCNReportsDao()
        ),
        objectMapper = ObjectMapper()
    )

    companion object {
        const val DATE_KEY = "date"
        const val INTERVAL_NUMBER_KEY = "intervalNumber"
        const val INTERVAL_LENGTH_MS_KEY = "intervalLengthMs"
    }

    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        logger.info("Processing request: ${context.awsRequestId}. " +
                "Query params: ${input.queryStringParameters}. " +
                "Body: ${input.body}")
        try {
            if (input.httpMethod == "GET") {
                logger.info("Handling GET Request for :${input.path}. ReqId: ${context.awsRequestId}")
                return handleGetReport(input)
            }
            logger.info("Handling POST Request for :${input.path}. ReqId: ${context.awsRequestId}")
            return handlePostReport(input)
        } catch (ex: Exception) {
            logger.info("Failed to serve request: ${context.awsRequestId}. Cause: ${ex.message}")
            return APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("CoEpi Service Internal Failure")
        }
    }

    fun handleGetReport(input: APIGatewayProxyRequestEvent): APIGatewayProxyResponseEvent {
        var statusCode: Int?
        var body: String?

        try {
            val (maybeDate, maybeInterval) = parseQueryParameters(input)

            logger.info("Querying reports with date: $maybeDate and intervalNumber: $maybeInterval")

            val reports = reportService.getReports(maybeDate.orNull(), maybeInterval.orNull())
                    .map {
                        record -> Base64.getEncoder().encodeToString(record.report)
                    }
            body = objectMapper.writeValueAsString(reports)
            statusCode = 200
            logger.info("Number of reports retrieved successfully: ${reports.size}")
        } catch (ex: TCNClientException) {
            logger.info("Failed to retrieve report due to client error", ex)
            body = ex.message
            statusCode = 400
        } catch (ex: UnexpectedIntervalLengthException) {
            logger.info("Failed to retrieve report due to bad intervalLength used by client", ex)
            body = ex.message
            statusCode = 401
        }
        return APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withBody(body)
    }

    private fun parseQueryParameters(input: APIGatewayProxyRequestEvent): Pair<Optional<LocalDate>, Optional<Long>> {
        val queryParameters = input.queryStringParameters
        var date = Optional.empty<LocalDate>()
        var intervalNumber = Optional.empty<Long>()

        if (queryParameters == null) return Pair(date, intervalNumber)


        try {
            if(queryParameters.containsKey(DATE_KEY) && !queryParameters[DATE_KEY].isNullOrEmpty()) {
                val rawDate = LocalDate.parse(queryParameters[DATE_KEY]) // Unit Test
                date = Optional.of(rawDate)
            }

            if(queryParameters.containsKey(INTERVAL_NUMBER_KEY)) {
                val rawBatch = queryParameters[INTERVAL_NUMBER_KEY]?.toLong()
                intervalNumber = if (rawBatch != null) Optional.of(rawBatch) else Optional.empty()

                if (!queryParameters.containsKey(INTERVAL_LENGTH_MS_KEY)) {
                    throw TCNClientException("$INTERVAL_LENGTH_MS_KEY query parameter is required if " +
                            " $INTERVAL_LENGTH_MS_KEY is provided")
                }
                val intervalLengthMs = queryParameters[INTERVAL_LENGTH_MS_KEY]?.toLong()

                if (intervalLengthMs != Intervals.INTERVAL_LENGTH_MS) {
                    throw UnexpectedIntervalLengthException("$intervalLengthMs is invalid for the date " +
                            "$DATE_KEY. Please use ${Intervals.INTERVAL_LENGTH_MS} to calculate $INTERVAL_NUMBER_KEY")
                }
            }
        } catch (ex: DateTimeParseException) {
            throw TCNClientException("$DATE_KEY in illegal date format.", ex)
        } catch (ex: NumberFormatException) {
            throw TCNClientException("$INTERVAL_NUMBER_KEY or $INTERVAL_LENGTH_MS_KEY in " +
                    "illegal number format.", ex)
        }

        if (intervalNumber.isPresent && intervalNumber.get() < 0) {
            throw TCNClientException("$INTERVAL_LENGTH_MS_KEY should be positive")
        }
        return Pair(date, intervalNumber)
    }

    fun handlePostReport(input: APIGatewayProxyRequestEvent): APIGatewayProxyResponseEvent {
        return try {
            val reportData = Base64.getDecoder().decode(input.body)

            val savedReport = reportService.saveReport(reportData)
            logger.info("Successfully added report ${savedReport.reportId}")

            APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
        } catch (ex: InvalidTCNSignatureException) {
            logger.info("Failed to put report due to illegal TCN Signature", ex)
            APIGatewayProxyResponseEvent()
                    .withBody(ex.message)
                    .withStatusCode(401)
        } catch (ex: IllegalArgumentException) {
            logger.info("Failed to put report due to client error", ex)
            APIGatewayProxyResponseEvent()
                    .withBody(ex.message)
                    .withStatusCode(400)
        }
    }
}