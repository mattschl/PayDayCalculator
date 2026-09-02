package ms.mattschlenkrich.paycalculator.ui.sync

import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncRecordAnalyzerTest {

    private lateinit var localDb: SQLiteDatabase
    private lateinit var remoteDb: SQLiteDatabase

    @Before
    fun setup() {
        localDb = SQLiteDatabase.create(null)
        remoteDb = SQLiteDatabase.create(null)

        setupTables(localDb)
        setupTables(remoteDb)
    }

    private fun setupTables(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE employers (employerId INTEGER PRIMARY KEY, employerName TEXT, employerIsDeleted INTEGER, employerUpdateTime TEXT)")
        db.execSQL("CREATE TABLE workPerformed (workPerformedId INTEGER PRIMARY KEY, wpDescription TEXT, wpIsDeleted INTEGER, wpUpdateTime TEXT)")
    }

    @After
    fun tearDown() {
        localDb.close()
        remoteDb.close()
    }

    @Test
    fun testIsDataDifferent_returnsTrue_whenDataDiffers() {
        val spec = TableSpec(
            "employers",
            listOf("employerName"),
            pkColumn = "employerId",
            updateTimeColumn = "employerUpdateTime"
        )

        remoteDb.execSQL("INSERT INTO employers (employerId, employerName, employerIsDeleted, employerUpdateTime) VALUES (1, 'Employer A', 0, '2024-01-01 10:00:00')")
        localDb.execSQL("INSERT INTO employers (employerId, employerName, employerIsDeleted, employerUpdateTime) VALUES (1, 'Employer B', 0, '2024-01-01 10:00:00')")

        val remoteCursor = remoteDb.rawQuery("SELECT * FROM employers", null)
        remoteCursor.moveToFirst()

        val localCursor = localDb.rawQuery("SELECT * FROM employers", null)
        localCursor.moveToFirst()

        assertTrue(isDataDifferent(localCursor, remoteCursor, spec))

        localCursor.close()
        remoteCursor.close()
    }

    @Test
    fun testIsDataDifferent_returnsFalse_whenDataMatches() {
        val spec = TableSpec(
            "employers",
            listOf("employerName"),
            pkColumn = "employerId",
            updateTimeColumn = "employerUpdateTime"
        )

        remoteDb.execSQL("INSERT INTO employers (employerId, employerName, employerIsDeleted, employerUpdateTime) VALUES (1, 'Employer A', 0, '2024-01-01 10:00:00')")
        localDb.execSQL("INSERT INTO employers (employerId, employerName, employerIsDeleted, employerUpdateTime) VALUES (1, 'Employer A', 0, '2024-01-01 10:00:00')")

        val remoteCursor = remoteDb.rawQuery("SELECT * FROM employers", null)
        remoteCursor.moveToFirst()

        val localCursor = localDb.rawQuery("SELECT * FROM employers", null)
        localCursor.moveToFirst()

        assertFalse(isDataDifferent(localCursor, remoteCursor, spec))

        localCursor.close()
        remoteCursor.close()
    }

    @Test
    fun testCheckRecordStatus_returnsNew_whenLocalMissing() {
        val spec = TableSpec(
            "employers",
            listOf("employerName"),
            pkColumn = "employerId",
            updateTimeColumn = "employerUpdateTime"
        )

        remoteDb.execSQL("INSERT INTO employers (employerId, employerName, employerIsDeleted, employerUpdateTime) VALUES (1, 'New Employer', 0, '2024-01-01 10:00:00')")

        val remoteCursor = remoteDb.rawQuery("SELECT * FROM employers", null)
        remoteCursor.moveToFirst()

        val status = checkRecordStatus(localDb, remoteDb, remoteCursor, spec)
        assertEquals(RecordStatus.NEW, status)

        remoteCursor.close()
    }

    @Test
    fun testCheckRecordStatus_returnsUpdated_whenRemoteNewer() {
        val spec = TableSpec(
            "employers",
            listOf("employerName"),
            pkColumn = "employerId",
            updateTimeColumn = "employerUpdateTime"
        )

        localDb.execSQL("INSERT INTO employers (employerId, employerName, employerIsDeleted, employerUpdateTime) VALUES (1, 'Employer A', 0, '2024-01-01 10:00:00')")
        remoteDb.execSQL("INSERT INTO employers (employerId, employerName, employerIsDeleted, employerUpdateTime) VALUES (2, 'Employer A', 0, '2024-01-01 12:00:00')")

        val remoteCursor = remoteDb.rawQuery("SELECT * FROM employers", null)
        remoteCursor.moveToFirst()

        val status = checkRecordStatus(localDb, remoteDb, remoteCursor, spec)
        assertEquals(RecordStatus.UPDATED, status)

        remoteCursor.close()
    }

    @Test
    fun testCheckRecordStatus_returnsExists_whenSameTimeAndData() {
        val spec = TableSpec(
            "employers",
            listOf("employerName"),
            pkColumn = "employerId",
            updateTimeColumn = "employerUpdateTime"
        )

        localDb.execSQL("INSERT INTO employers (employerId, employerName, employerIsDeleted, employerUpdateTime) VALUES (1, 'Employer A', 0, '2024-01-01 10:00:00')")
        remoteDb.execSQL("INSERT INTO employers (employerId, employerName, employerIsDeleted, employerUpdateTime) VALUES (2, 'Employer A', 0, '2024-01-01 10:00:00')")

        val remoteCursor = remoteDb.rawQuery("SELECT * FROM employers", null)
        remoteCursor.moveToFirst()

        val status = checkRecordStatus(localDb, remoteDb, remoteCursor, spec)
        assertEquals(RecordStatus.EXISTS, status)

        remoteCursor.close()
    }

    @Test
    fun testBuildWhereClause_handlesNaturalKeys() {
        val spec = TableSpec("employers", listOf("employerName"))

        remoteDb.execSQL("INSERT INTO employers (employerId, employerName) VALUES (1, ' Search Me ')")
        val remoteCursor = remoteDb.rawQuery("SELECT * FROM employers", null)
        remoteCursor.moveToFirst()

        val whereData = buildWhereClause(localDb, remoteDb, remoteCursor, spec, emptyMap())

        assertTrue(whereData != null)
        assertEquals("TRIM(employerName) = ?", whereData?.first)
        assertEquals("Search Me", whereData?.second?.get(0))

        remoteCursor.close()
    }
}