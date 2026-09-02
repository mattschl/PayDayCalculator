package ms.mattschlenkrich.paycalculator.ui.sync

import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncDataResolverTest {

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
        db.execSQL("CREATE TABLE workPerformed (workPerformedId INTEGER PRIMARY KEY, wpDescription TEXT, wpIsDeleted INTEGER, wpUpdateTime TEXT, wpEmployerId INTEGER)")
    }

    @After
    fun tearDown() {
        localDb.close()
        remoteDb.close()
    }

    @Test
    fun testGetLocalFkValue_returnsLocalId_basedOnNaturalKey() {
        // Setup local employer
        localDb.execSQL("INSERT INTO employers (employerId, employerName) VALUES (10, 'Employer X')")

        // Setup remote employer with same name but different ID
        remoteDb.execSQL("INSERT INTO employers (employerId, employerName) VALUES (20, 'Employer X')")

        // Remote record using the remote ID
        remoteDb.execSQL("INSERT INTO workPerformed (workPerformedId, wpDescription, wpEmployerId) VALUES (1, 'Task 1', 20)")
        val remoteCursor = remoteDb.rawQuery("SELECT * FROM workPerformed", null)
        remoteCursor.moveToFirst()

        val fkSpec = FKSpec("wpEmployerId", "employers", "employerId", "employerName")
        val fkIndex = remoteCursor.getColumnIndex("wpEmployerId")

        val localFkValue =
            getLocalFkValue(localDb, remoteDb, remoteCursor, fkSpec, fkIndex, emptyMap())

        assertEquals("10", localFkValue)

        remoteCursor.close()
    }

    @Test
    fun testGetContentValues_remapsFks() {
        val spec = TableSpec(
            "workPerformed",
            listOf("wpDescription"),
            listOf(FKSpec("wpEmployerId", "employers", "employerId", "employerName")),
            pkColumn = "workPerformedId",
            updateTimeColumn = "wpUpdateTime"
        )

        // Setup local employer
        localDb.execSQL("INSERT INTO employers (employerId, employerName) VALUES (100, 'Test Employer')")

        // Setup remote employer with different ID
        remoteDb.execSQL("INSERT INTO employers (employerId, employerName) VALUES (200, 'Test Employer')")

        // Remote record using remote employer ID
        remoteDb.execSQL("INSERT INTO workPerformed (workPerformedId, wpDescription, wpEmployerId, wpUpdateTime) VALUES (1, 'Clean', 200, '2024-02-01 10:00:00')")
        val remoteCursor = remoteDb.rawQuery("SELECT * FROM workPerformed", null)
        remoteCursor.moveToFirst()

        val contentValues = getContentValues(localDb, remoteDb, remoteCursor, spec, emptyMap())

        assertEquals(100L, contentValues.getAsLong("wpEmployerId"))
        assertEquals("Clean", contentValues.getAsString("wpDescription"))

        remoteCursor.close()
    }
}