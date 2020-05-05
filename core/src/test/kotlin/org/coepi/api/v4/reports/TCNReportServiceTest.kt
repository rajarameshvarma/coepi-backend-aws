package org.coepi.api.v4.reports

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.assertj.core.api.Assertions.assertThat
import org.coepi.api.Fixtures
import org.coepi.api.common.time.toUtcLocalDate
import org.coepi.api.common.toByteBuffer
import org.coepi.api.v4.dao.TCNReportRecord
import org.coepi.api.v4.dao.TCNReportsDao
import org.coepi.api.v4.toInterval
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer

class TCNReportServiceTest {

    private val now = Instant.parse("2000-11-22T13:44:55.666Z")

    private val clock = Clock.fixed(now, ZoneId.of("UTC"))
    private val reportsDao = mockk<TCNReportsDao>()

    private val subject = TCNReportService(clock, reportsDao)

    @Test
    fun `getReports should use provided non-null arguments`() {
        // GIVEN
        val date = LocalDate.of(1987, 7, 11)
        val intervalNumber = 8765309L

        val expectedReports = mockk<List<TCNReportRecord>>()
        every { reportsDao.queryReports(any(), any()) } returns expectedReports

        // WHEN
        val actualReports = subject.getReports(date, intervalNumber)

        // THEN
        verify {
            reportsDao.queryReports(date, intervalNumber)
        }

        assertThat(actualReports).isEqualTo(expectedReports)
    }

    @Test
    fun `getReports should use current date and intervalNumber if none provided`() {
        // GIVEN
        val expectedReports = mockk<List<TCNReportRecord>>()
        every { reportsDao.queryReports(any(), any()) } returns expectedReports

        // WHEN
        val actualReports = subject.getReports(null, null)

        // THEN
        verify {
            reportsDao.queryReports(now.toUtcLocalDate(), now.toInterval())
        }

        assertThat(actualReports).isEqualTo(expectedReports)
    }

    @Test
    fun `saveReport should save the report based on the current time`() {
        // GIVEN
        val reportData = ByteBuffer.wrap(Fixtures.someBytes())

        val expectedSavedReport = mockk<TCNReportRecord>()
        every { reportsDao.addReport(any(), any(), any(), any()) } returns expectedSavedReport

        // WHEN
        val actualSavedReport = subject.saveReport(reportData)

        // THEN
        verify {
            reportsDao.addReport(
                reportData,
                now.toUtcLocalDate(),
                now.toInterval(),
                now.toEpochMilli()
            )
        }
        assertThat(actualSavedReport).isEqualTo(expectedSavedReport)
    }
}
