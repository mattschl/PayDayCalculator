package ms.mattschlenkrich.paycalculator.ui.sync

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.TABLE_EMPLOYERS
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseSyncHelperTest {

    private lateinit var appDb: PayDatabase
    private lateinit var backupDb: SQLiteDatabase
    private lateinit var syncHelper: DatabaseSyncHelper
    private val df = DateFunctions()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        appDb = Room.inMemoryDatabaseBuilder(context, PayDatabase::class.java).build()
        backupDb = SQLiteDatabase.create(null)
        setupBackupTables(backupDb)

        syncHelper = DatabaseSyncHelper(
            appDb = appDb,
            df = df,
            deviceId = 123L,
            onConflict = { ConflictChoice.KEEP_DRIVE },
            onSyncError = { println("Sync Error: $it") },
            isRestore = false
        )
    }

    private fun setupBackupTables(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_EMPLOYERS (" +
                    "employerId INTEGER PRIMARY KEY, " +
                    "employerName TEXT, " +
                    "payFrequency TEXT, " +
                    "startDate TEXT, " +
                    "dayOfWeek TEXT, " +
                    "cutoffDaysBefore INTEGER, " +
                    "midMonthlyDate INTEGER, " +
                    "mainMonthlyDate INTEGER, " +
                    "employerIsDeleted INTEGER, " +
                    "employerUpdateTime TEXT)"
        )
    }

    @After
    fun tearDown() {
        appDb.close()
        backupDb.close()
    }

    @Test
    fun testSyncEmployers_insertsNewEmployer() = runBlocking {
        backupDb.execSQL(
            "INSERT INTO $TABLE_EMPLOYERS " +
                    "(employerId, employerName, payFrequency, startDate, dayOfWeek, cutoffDaysBefore, midMonthlyDate, mainMonthlyDate, employerIsDeleted, employerUpdateTime) " +
                    "VALUES (1, 'New Employer', 'Weekly', '2024-01-01', 'Monday', 0, 0, 0, 0, '2024-01-01 10:00:00')"
        )

        val result = syncHelper.syncEmployers(backupDb)
        assertEquals(1, result.first) // 1 insert
        assertEquals(0, result.second) // 0 updates

        val localEmployer = appDb.getEmployerDao().getEmployerSync(1)
        assertNotNull(localEmployer)
        assertEquals("New Employer", localEmployer?.employerName)
    }

    @Test
    fun testSyncEmployers_updatesNewerEmployer() = runBlocking {
        val oldEmployer = Employers(
            1, "Employer X", "Weekly", "2024-01-01", "Monday", 0, 0, 0, false, "2024-01-01 09:00:00"
        )
        appDb.getEmployerDao().insertEmployer(oldEmployer)

        backupDb.execSQL(
            "INSERT INTO $TABLE_EMPLOYERS " +
                    "(employerId, employerName, payFrequency, startDate, dayOfWeek, cutoffDaysBefore, midMonthlyDate, mainMonthlyDate, employerIsDeleted, employerUpdateTime) " +
                    "VALUES (1, 'Updated Name', 'Weekly', '2024-01-01', 'Monday', 0, 0, 0, 0, '2024-01-01 10:00:00')"
        )

        val result = syncHelper.syncEmployers(backupDb)
        assertEquals(0, result.first) // 0 inserts
        assertEquals(1, result.second) // 1 update

        val localEmployer = appDb.getEmployerDao().getEmployerSync(1)
        assertEquals("Updated Name", localEmployer?.employerName)
    }

    @Test
    fun testSyncEmployers_ignoresOlderEmployer() = runBlocking {
        val newEmployer = Employers(
            1,
            "Modern Name",
            "Weekly",
            "2024-01-01",
            "Monday",
            0,
            0,
            0,
            false,
            "2024-01-01 12:00:00"
        )
        appDb.getEmployerDao().insertEmployer(newEmployer)

        backupDb.execSQL(
            "INSERT INTO $TABLE_EMPLOYERS " +
                    "(employerId, employerName, payFrequency, startDate, dayOfWeek, cutoffDaysBefore, midMonthlyDate, mainMonthlyDate, employerIsDeleted, employerUpdateTime) " +
                    "VALUES (1, 'Old Name', 'Weekly', '2024-01-01', 'Monday', 0, 0, 0, 0, '2024-01-01 10:00:00')"
        )

        val result = syncHelper.syncEmployers(backupDb)
        assertEquals(0, result.first)
        assertEquals(0, result.second)

        val localEmployer = appDb.getEmployerDao().getEmployerSync(1)
        assertEquals("Modern Name", localEmployer?.employerName)
    }

    @Test
    fun testSyncEmployers_handlesConflict_keepDrive() = runBlocking {
        // Local has "Employer A" with ID 1
        val localEmployer = Employers(
            1, "Employer A", "Weekly", "2024-01-01", "Monday", 0, 0, 0, false, "2024-01-01 09:00:00"
        )
        appDb.getEmployerDao().insertEmployer(localEmployer)

        // Backup has "Employer A" but with ID 2 (Conflict!)
        backupDb.execSQL(
            "INSERT INTO $TABLE_EMPLOYERS " +
                    "(employerId, employerName, payFrequency, startDate, dayOfWeek, cutoffDaysBefore, midMonthlyDate, mainMonthlyDate, employerIsDeleted, employerUpdateTime) " +
                    "VALUES (2, 'Employer A', 'Weekly', '2024-01-01', 'Monday', 0, 0, 0, 0, '2024-01-01 10:00:00')"
        )

        val result = syncHelper.syncEmployers(backupDb)

        assertEquals(1, result.first) // 1 insert (remote)

        val employer2 = appDb.getEmployerDao().getEmployerSync(2)
        assertNotNull(employer2)
        assertEquals("Employer A", employer2?.employerName)

        val employer1 = appDb.getEmployerDao().getEmployerSync(1)
        assertNotNull(employer1)
        assertTrue(employer1?.employerName?.contains("LOCAL") == true)
    }

    @Test
    fun testSyncEmployers_isRestore_overwritesEverything() = runBlocking {
        // Local has "Employer A" with ID 1, newer than backup
        val localEmployer = Employers(
            1, "Local Name", "Weekly", "2024-01-01", "Monday", 0, 0, 0, false, "2024-01-01 12:00:00"
        )
        appDb.getEmployerDao().insertEmployer(localEmployer)

        // Backup has "Backup Name" with same ID 1, older than local
        backupDb.execSQL(
            "INSERT INTO $TABLE_EMPLOYERS " +
                    "(employerId, employerName, payFrequency, startDate, dayOfWeek, cutoffDaysBefore, midMonthlyDate, mainMonthlyDate, employerIsDeleted, employerUpdateTime) " +
                    "VALUES (1, 'Backup Name', 'Weekly', '2024-01-01', 'Monday', 0, 0, 0, 0, '2024-01-01 10:00:00')"
        )

        // Create a new helper with isRestore = true
        val restoreHelper = DatabaseSyncHelper(
            appDb = appDb,
            df = df,
            deviceId = 123L,
            onConflict = { ConflictChoice.KEEP_DRIVE },
            onSyncError = { },
            isRestore = true
        )

        val result = restoreHelper.syncEmployers(backupDb)
        assertEquals(0, result.first)
        assertEquals(1, result.second) // 1 update forced by isRestore

        val localAfter = appDb.getEmployerDao().getEmployerSync(1)
        assertEquals("Backup Name", localAfter?.employerName)
    }
}