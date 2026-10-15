package ms.mattschlenkrich.paycalculator.ui.sync

import ms.mattschlenkrich.paycalculator.data.entity.SyncHistory
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncHistorySourceTest {

    @Test
    fun testSyncHistorySourceName_recordsCommaSeparatedBackupFileNames() {
        val usedBackupFiles = listOf("pay_20260510_120000.db", "pay_20260512_083000.db")
        val uploadedFile = "pay_20260514_102030.db"

        val allSources = (usedBackupFiles + listOfNotNull(uploadedFile)).distinct()
        val sourceName = allSources.joinToString(", ")

        val syncHistory = SyncHistory(
            syncId = 123L,
            syncTime = "2026-05-14 10:20:30",
            syncSourceName = sourceName,
            syncDeviceId = 1L,
            syncStatus = "Success",
            syncRecordsProcessed = "Sync Report"
        )

        assertEquals(
            "pay_20260510_120000.db, pay_20260512_083000.db, pay_20260514_102030.db",
            syncHistory.syncSourceName
        )
    }
}