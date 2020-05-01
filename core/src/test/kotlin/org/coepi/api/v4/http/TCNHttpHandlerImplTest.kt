package org.coepi.api.v4.http

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.coepi.api.Fixtures
import org.coepi.api.common.toByteBuffer
import org.coepi.api.v4.reports.TCNReportService
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class TCNHttpHandlerImplTest {

    val reportService = mockk<TCNReportService>()

    val subject = TCNHttpHandlerImpl(ObjectMapper(), reportService)

    @Test
    fun `getReport should return Ok with valid inputs`() {
        // GIVEN
        val expectedDate = LocalDate.of(1999, 10, 31)
        val parameters = mapOf(
            "date" to "1999-10-31"
        )

        every { reportService.getReports(any(), any()) } returns emptyList()

        // WHEN
        val response = subject.getReport(parameters)

        // THEN
        verify { reportService.getReports(expectedDate, null) }
        assertThat(response).isInstanceOf(Ok::class.java)
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

