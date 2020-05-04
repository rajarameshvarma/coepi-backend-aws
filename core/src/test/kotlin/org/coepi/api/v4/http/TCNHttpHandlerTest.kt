package org.coepi.api.v4.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.ReferenceType
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.coepi.api.Fixtures
import org.coepi.api.common.toByteBuffer
import org.coepi.api.v4.dao.TCNReportRecord
import org.coepi.api.v4.reports.TCNReportService
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.time.LocalDate
import java.util.*

class TCNHttpHandlerTest {

    val reportService = mockk<TCNReportService>()
    val objectMapper = ObjectMapper()

    val subject = TCNHttpHandler(objectMapper, reportService)

    @Test
    fun `getReport should return Ok with valid inputs`() {
        // GIVEN
        val expectedDate = LocalDate.of(1999, 10, 31)
        val parameters = mapOf(
            "date" to "1999-10-31"
        )
        val expectedReports = listOf(
            TCNReportRecord(
                report = "<report data>".toByteArray()
            )
        )

        every { reportService.getReports(any(), any()) } returns expectedReports

        // WHEN
        val response = subject.getReport(parameters)

        // THEN
        verify { reportService.getReports(expectedDate, null) }
        assertThat(response).isInstanceOf(Ok::class.java)

        val deserializedResponse = objectMapper.readValue<List<ByteArray>>(response.body!!.array())

        assertThat(deserializedResponse).hasSize(expectedReports.size)

        deserializedResponse
            .zip(expectedReports)
            .forEach { (actual, expected) -> assertThat(actual).isEqualTo(expected.report) }
    }

    @Test
    fun `postReport should return Ok with valid inputs`() {
        // GIVEN
        val report = Fixtures.someBytes()
        val body = Base64.getEncoder().encode(report).toByteBuffer()

        every { reportService.saveReport(any()) } returns Fixtures.mockSavedReport()

        // WHEN
        val response = subject.postReport(body)

        // THEN
        verify { reportService.saveReport(report.toByteBuffer()) }
        assertThat(response).isInstanceOf(Ok::class.java)
    }
}

