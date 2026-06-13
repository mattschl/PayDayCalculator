package ms.mattschlenkrich.paycalculator.ui.sync

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.core.database.sqlite.transaction
import ms.mattschlenkrich.paycalculator.common.PAY_DB_NAME
import java.io.File

private const val TAG = "MergeHelper"

class MergeHelper(private val context: Context, private val remoteDbPath: String) {

    /**
     * Analyzes the remote database and compares it with the local one.
     * Returns a summary of records that are present in the remote DB but not locally.
     */
    fun getSyncSummary(): String {
        val summary = StringBuilder()
        var remoteDb: SQLiteDatabase? = null
        var localDb: SQLiteDatabase? = null

        try {
            val remoteFile = File(remoteDbPath)
            if (!remoteFile.exists()) return "Remote database file not found: $remoteDbPath"

            val localFile = context.getDatabasePath(PAY_DB_NAME)
            if (!localFile.exists()) return "Local database not found."

            remoteDb = SQLiteDatabase.openDatabase(remoteDbPath, null, SQLiteDatabase.OPEN_READONLY)
            localDb = SQLiteDatabase.openDatabase(
                localFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )

            summary.append("DEBUG INFO:\n")
            summary.append("  Remote file: ${remoteFile.name} (${remoteFile.length() / 1024} KB)\n")
            summary.append("  Local file: ${localFile.name} (${localFile.length() / 1024} KB)\n\n")

            val lookbackTime = getLookbackTime(localDb)
            if (lookbackTime == "1970-01-01 00:00:00") {
                summary.append("  Safety Window: Full Restore mode enabled (no previous history found)\n\n")
            } else {
                summary.append("  Safety Window: Analyzing records updated since: $lookbackTime (Optimized Global Baseline)\n\n")
            }

            val tables = getTables()
            var totalNewRecords = 0
            var totalUpdatedRecords = 0
            for (spec in tables) {
                if (!isTableExists(localDb, spec.tableName) || !isTableExists(
                        remoteDb,
                        spec.tableName
                    )
                ) {
                    Log.w(
                        TAG,
                        "Skipping table ${spec.tableName} as it is missing in one of the databases."
                    )
                    continue
                }

                val results = findNewAndUpdatedRecords(localDb, remoteDb, spec, lookbackTime)
                val newRecords = results.first
                val updatedRecords = results.second

                if (newRecords.isNotEmpty() || updatedRecords.isNotEmpty()) {
                    totalNewRecords += newRecords.size
                    totalUpdatedRecords += updatedRecords.size
                    summary.append("${spec.tableName.uppercase()}:\n")
                    if (newRecords.isNotEmpty()) {
                        summary.append("  Found ${newRecords.size} new records.\n")
                        newRecords.take(3).forEach { summary.append("    - [NEW] $it\n") }
                    }
                    if (updatedRecords.isNotEmpty()) {
                        summary.append("  Found ${updatedRecords.size} updated records.\n")
                        updatedRecords.take(3).forEach { summary.append("    - [UPD] $it\n") }
                    }
                    summary.append("\n")
                }
            }

            if (totalNewRecords == 0 && totalUpdatedRecords == 0) {
                summary.append("No changes found in the backup. Your local database is up to date.")
            } else {
                val header = StringBuilder("SYNC ANALYSIS COMPLETE\n")
                if (totalNewRecords > 0) header.append("Total new records: $totalNewRecords\n")
                if (totalUpdatedRecords > 0) header.append("Total updated records: $totalUpdatedRecords\n")
                header.append("\n")
                summary.insert(0, header.toString())
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing databases", e)
            summary.append("Analysis failed: ${e.message}")
        } finally {
            remoteDb?.close()
            localDb?.close()
        }
        return summary.toString()
    }

    /**
     * Applies the synchronization from the remote database to the local one.
     */
    fun applySync(onProgress: (Int, Int) -> Unit = { _, _ -> }): String {
        val summary = StringBuilder()
        var remoteDb: SQLiteDatabase? = null
        var localDb: SQLiteDatabase? = null
        val idMap = mutableMapOf<String, MutableMap<Long, Long>>()

        try {
            val remoteFile = File(remoteDbPath)
            if (!remoteFile.exists()) return "Remote database file not found."

            val localFile = context.getDatabasePath(PAY_DB_NAME)
            remoteDb = SQLiteDatabase.openDatabase(remoteDbPath, null, SQLiteDatabase.OPEN_READONLY)
            localDb = SQLiteDatabase.openDatabase(
                localFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE
            )

            val lookbackTime = getLookbackTime(localDb)
            val tables = getTables()
            var totalNew = 0
            var totalUpdated = 0
            val mismatchTables = mutableListOf<String>()
            val tableSummaries = StringBuilder()

            for ((index, spec) in tables.withIndex()) {
                onProgress(index, tables.size)

                if (!isTableExists(localDb, spec.tableName) || !isTableExists(
                        remoteDb,
                        spec.tableName
                    )
                ) {
                    Log.w(
                        TAG,
                        "Skipping table ${spec.tableName} as it is missing in one of the databases."
                    )
                    continue
                }

                idMap[spec.tableName] = mutableMapOf()

                var tableNewCount = 0
                var tableUpdatedCount = 0
                var remoteCount = 0

                localDb.transaction {
                    try {
                        val query = if (spec.updateTimeColumn != null) {
                            "SELECT * FROM ${spec.tableName} WHERE ${spec.updateTimeColumn} > '$lookbackTime'"
                        } else {
                            "SELECT * FROM ${spec.tableName}"
                        }

                        val remoteCursor = remoteDb.rawQuery(query, null)
                        remoteCount = remoteCursor.count

                        if (remoteCursor.moveToFirst()) {
                            do {
                                val remotePkValue = if (spec.pkColumn != null) {
                                    val idx = remoteCursor.getColumnIndex(spec.pkColumn)
                                    if (idx != -1 && remoteCursor.getType(idx) == Cursor.FIELD_TYPE_INTEGER) {
                                        remoteCursor.getLong(idx)
                                    } else -1L
                                } else -1L

                                val (status, localId) = checkRecordStatusWithId(
                                    this,
                                    remoteDb,
                                    remoteCursor,
                                    spec,
                                    idMap
                                )

                                if (status == RecordStatus.EXISTS) {
                                    if (remotePkValue != -1L) {
                                        idMap[spec.tableName]!![remotePkValue] = localId
                                    }
                                    continue
                                }

                                val values = getContentValues(
                                    this,
                                    remoteDb,
                                    remoteCursor,
                                    spec,
                                    idMap,
                                    localId
                                )
                                val newId = insertWithOnConflict(
                                    spec.tableName,
                                    null,
                                    values,
                                    SQLiteDatabase.CONFLICT_REPLACE
                                )

                                if (newId != -1L) {
                                    val mappingId =
                                        if (spec.pkColumn != null && values.containsKey(spec.pkColumn)) {
                                            values.getAsLong(spec.pkColumn) ?: newId
                                        } else {
                                            newId
                                        }

                                    if (remotePkValue != -1L) {
                                        idMap[spec.tableName]!![remotePkValue] = mappingId
                                    }

                                    if (status == RecordStatus.NEW) tableNewCount++ else tableUpdatedCount++
                                } else {
                                    Log.e(
                                        TAG,
                                        "Failed to sync record in ${spec.tableName}: $values"
                                    )
                                }
                            } while (remoteCursor.moveToNext())
                        }
                        remoteCursor.close()
                        totalNew += tableNewCount
                        totalUpdated += tableUpdatedCount
                    } finally {
                    }
                }

                // Verification: ensure local count is at least the remote count
                val localCountCursor =
                    localDb.rawQuery("SELECT COUNT(*) FROM ${spec.tableName}", null)
                var localCount = 0
                if (localCountCursor.moveToFirst()) {
                    localCount = localCountCursor.getInt(0)
                }
                localCountCursor.close()

                if (localCount < remoteCount && spec.tableName != "syncHistory") {
                    mismatchTables.add(spec.tableName)
                }

                tableSummaries.append("  - ${spec.tableName}: Backup: $remoteCount, Local: $localCount\n")
            }

            summary.append("Sync completed.\n")
            summary.append("Inserted $totalNew new records.\n")
            summary.append("Updated $totalUpdated existing records.\n\n")
            summary.append("TABLE STATISTICS:\n")
            summary.append(tableSummaries.toString())

            if (mismatchTables.isNotEmpty()) {
                summary.append("\nWARNING: Data mismatch found in tables:\n")
                mismatchTables.forEach { summary.append("- $it\n") }
            } else {
                summary.append("\nRecord count verification passed for all tables.")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Sync application failed", e)
            summary.append("Sync failed: ${e.message}")
        } finally {
            remoteDb?.close()
            localDb?.close()
        }
        return summary.toString()
    }
}