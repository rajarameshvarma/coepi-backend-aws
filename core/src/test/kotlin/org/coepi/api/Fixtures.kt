package org.coepi.api

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.ObjectAssert
import org.coepi.api.v4.dao.TCNReportRecord

object Fixtures {
     fun someBytes() = ByteArray(42) { it.toByte() }

     fun mockSavedReport(): TCNReportRecord = mockk {
          every { reportId } returns "<mock report id>"
     }
}
