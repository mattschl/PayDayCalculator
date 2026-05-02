package ms.mattschlenkrich.paycalculator.ui.sync

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import ms.mattschlenkrich.paycalculator.common.PAY_DB_NAME
import java.io.File
import androidx.core.database.sqlite.transaction
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

            val tables = getTables()
            var totalNewRecords = 0
            var totalUpdatedRecords = 0
            for (spec in tables) {
                val results = findNewAndUpdatedRecords(localDb, remoteDb, spec)
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

            val tables = getTables()
            var totalNew = 0
            var totalUpdated = 0
            val mismatchTables = mutableListOf<String>()
            val tableSummaries = StringBuilder()

            for ((index, spec) in tables.withIndex()) {
                onProgress(index, tables.size)
                idMap[spec.tableName] = mutableMapOf()

                var tableNewCount = 0
                var tableUpdatedCount = 0
                var remoteCount = 0

                localDb.transaction {
                    try {
                        val remoteCursor =
                            remoteDb.rawQuery("SELECT * FROM ${spec.tableName}", null)
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

    private fun getContentValues(
        localDb: SQLiteDatabase,
        remoteDb: SQLiteDatabase,
        cursor: Cursor,
        spec: TableSpec,
        idMap: Map<String, Map<Long, Long>>,
        localId: Long = -1L
    ): ContentValues {
        val values = ContentValues()

        // 1. Resolve local employer ID if this record depends on an employer
        var recordLocalEmployerId: Long? = null
        if (spec.employerIdColumn != null) {
            val idx = cursor.getColumnIndex(spec.employerIdColumn)
            if (idx != -1 && !cursor.isNull(idx)) {
                // Employer ID column is always an FK to 'employers'
                val employerFk =
                    FKSpec(spec.employerIdColumn, "employers", "employerId", "employerName")
                recordLocalEmployerId = getLocalFkValue(
                    localDb, remoteDb, cursor, employerFk, idx, idMap
                ).toLongOrNull()
            }
        }

        for (i in 0 until cursor.columnCount) {
            val colName = cursor.getColumnName(i)

            if (colName == spec.pkColumn) {
                if (localId != -1L) {
                    values.put(colName, localId)
                } else if (!cursor.isNull(i)) {
                    values.put(colName, cursor.getLong(i))
                }
                continue
            }

            val fk = spec.fks.find { it.fkColumn == colName }
            if (fk != null && !cursor.isNull(i)) {
                val localFkValue = getLocalFkValue(
                    localDb, remoteDb, cursor, fk, i, idMap, recordLocalEmployerId
                )
                if (localFkValue != "-1") {
                    values.put(colName, localFkValue)
                    continue
                }
            }

            if (cursor.isNull(i)) {
                values.putNull(colName)
            } else {
                when (cursor.getType(i)) {
                    Cursor.FIELD_TYPE_INTEGER -> values.put(colName, cursor.getLong(i))
                    Cursor.FIELD_TYPE_FLOAT -> values.put(colName, cursor.getDouble(i))
                    Cursor.FIELD_TYPE_STRING -> values.put(colName, cursor.getString(i))
                    Cursor.FIELD_TYPE_BLOB -> values.put(colName, cursor.getBlob(i))
                    else -> values.putNull(colName)
                }
            }
        }
        return values
    }

    private fun getLocalFkValue(
        localDb: SQLiteDatabase,
        remoteDb: SQLiteDatabase,
        cursor: Cursor,
        fk: FKSpec,
        columnIndex: Int,
        idMap: Map<String, Map<Long, Long>>,
        recordEmployerId: Long? = null
    ): String {
        if (cursor.getType(columnIndex) != Cursor.FIELD_TYPE_INTEGER) {
            return cursor.getString(columnIndex)
        }

        val remoteFkId = cursor.getLong(columnIndex)
        val localFkIdFromMap = idMap[fk.parentTable]?.get(remoteFkId)
        if (localFkIdFromMap != null) return localFkIdFromMap.toString()

        val parentName = getNameFromTable(
            remoteDb,
            fk.parentTable,
            fk.parentPk,
            fk.parentNaturalKey,
            remoteFkId
        )

        val localFkId = if (fk.dependsOnEmployer && recordEmployerId != null) {
            getIdByName(
                localDb, fk.parentTable, fk.parentPk, fk.parentNaturalKey,
                parentName, recordEmployerId
            )
        } else {
            getIdByName(
                localDb, fk.parentTable, fk.parentPk, fk.parentNaturalKey,
                parentName
            )
        }

        return localFkId.toString()
    }

    private fun findNewAndUpdatedRecords(
        localDb: SQLiteDatabase,
        remoteDb: SQLiteDatabase,
        spec: TableSpec
    ): Pair<List<String>, List<String>> {
        val newItems = mutableListOf<String>()
        val updatedItems = mutableListOf<String>()
        val query = "SELECT * FROM ${spec.tableName}"

        try {
            val cursor = remoteDb.rawQuery(query, null)
            if (cursor.moveToFirst()) {
                do {
                    val status = checkRecordStatus(localDb, remoteDb, cursor, spec)
                    val displayName = try {
                        if (spec.keys.isNotEmpty()) {
                            cursor.getString(cursor.getColumnIndexOrThrow(spec.keys[0])).trim()
                        } else if (spec.fks.isNotEmpty()) {
                            val fk = spec.fks[0]
                            getNameFromTable(
                                remoteDb,
                                fk.parentTable,
                                fk.parentPk,
                                fk.parentNaturalKey,
                                cursor.getLong(cursor.getColumnIndexOrThrow(fk.fkColumn))
                            )
                        } else {
                            "Unknown record"
                        }
                    } catch (e: Exception) {
                        "Unknown record: $e"
                    }

                    when (status) {
                        RecordStatus.NEW -> newItems.add(displayName)
                        RecordStatus.UPDATED -> updatedItems.add(displayName)
                        RecordStatus.EXISTS -> {}
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            Log.w(TAG, "Table ${spec.tableName} query failed: ${e.message}")
        }
        return Pair(newItems, updatedItems)
    }

    private enum class RecordStatus { NEW, UPDATED, EXISTS }

    private fun checkRecordStatus(
        localDb: SQLiteDatabase,
        remoteDb: SQLiteDatabase,
        remoteCursor: Cursor,
        spec: TableSpec
    ): RecordStatus {
        return checkRecordStatusWithId(localDb, remoteDb, remoteCursor, spec, emptyMap()).first
    }

    private fun checkRecordStatusWithId(
        localDb: SQLiteDatabase,
        remoteDb: SQLiteDatabase,
        remoteCursor: Cursor,
        spec: TableSpec,
        idMap: Map<String, Map<Long, Long>>
    ): Pair<RecordStatus, Long> {
        val whereData = buildWhereClause(localDb, remoteDb, remoteCursor, spec, idMap)
            ?: return Pair(RecordStatus.NEW, -1L)

        val localCursor = localDb.query(
            spec.tableName,
            null, // Retrieve all columns for thorough comparison
            whereData.first,
            whereData.second,
            null, null, null
        )

        var status = RecordStatus.NEW
        var localId = -1L

        if (localCursor.moveToFirst()) {
            if (spec.pkColumn != null) {
                localId = localCursor.getLong(localCursor.getColumnIndexOrThrow(spec.pkColumn))
            }

            if (spec.updateTimeColumn != null) {
                val localUpdateTime =
                    localCursor.getString(localCursor.getColumnIndexOrThrow(spec.updateTimeColumn))
                val remoteUpdateTime =
                    remoteCursor.getString(remoteCursor.getColumnIndexOrThrow(spec.updateTimeColumn))

                if (remoteUpdateTime != null && (localUpdateTime == null || remoteUpdateTime > localUpdateTime)) {
                    status = RecordStatus.UPDATED
                } else if (remoteUpdateTime != null && remoteUpdateTime == localUpdateTime) {
                    // Timestamps are equal, update only if data actually differs
                    if (isDataDifferent(localCursor, remoteCursor, spec)) {
                        status = RecordStatus.UPDATED
                    } else {
                        status = RecordStatus.EXISTS
                    }
                } else {
                    // Local record is newer or remoteUpdateTime is null
                    status = RecordStatus.EXISTS
                }
            } else {
                // No timestamp available, must compare all data columns
                status = if (isDataDifferent(localCursor, remoteCursor, spec)) {
                    RecordStatus.UPDATED
                } else {
                    RecordStatus.EXISTS
                }
            }
        }
        localCursor.close()
        return Pair(status, localId)
    }

    /**
     * Deep comparison of record data between local and remote cursors.
     * Ignores PK and update time columns.
     */
    private fun isDataDifferent(
        localCursor: Cursor,
        remoteCursor: Cursor,
        spec: TableSpec
    ): Boolean {
        for (i in 0 until remoteCursor.columnCount) {
            val colName = remoteCursor.getColumnName(i)
            if (colName == spec.pkColumn || colName == spec.updateTimeColumn) continue

            val remoteIdx = i
            val localIdx = localCursor.getColumnIndex(colName)
            if (localIdx == -1) continue

            if (remoteCursor.isNull(remoteIdx) != localCursor.isNull(localIdx)) return true
            if (remoteCursor.isNull(remoteIdx)) continue

            val type = remoteCursor.getType(remoteIdx)
            if (type != localCursor.getType(localIdx)) return true

            val isDifferent = when (type) {
                Cursor.FIELD_TYPE_INTEGER -> remoteCursor.getLong(remoteIdx) != localCursor.getLong(
                    localIdx
                )

                Cursor.FIELD_TYPE_FLOAT -> remoteCursor.getDouble(remoteIdx) != localCursor.getDouble(
                    localIdx
                )

                Cursor.FIELD_TYPE_STRING -> remoteCursor.getString(remoteIdx) != localCursor.getString(
                    localIdx
                )

                Cursor.FIELD_TYPE_BLOB -> !remoteCursor.getBlob(remoteIdx)
                    .contentEquals(localCursor.getBlob(localIdx))

                else -> false
            }
            if (isDifferent) return true
        }
        return false
    }

    private fun buildWhereClause(
        localDb: SQLiteDatabase,
        remoteDb: SQLiteDatabase,
        remoteCursor: Cursor,
        spec: TableSpec,
        idMap: Map<String, Map<Long, Long>>
    ): Pair<String, Array<String>>? {
        val whereClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()
        val handledColumns = mutableSetOf<String>()

        // 1. Resolve local employer ID if this record depends on an employer
        var recordLocalEmployerId: Long? = null
        if (spec.employerIdColumn != null) {
            val idx = remoteCursor.getColumnIndex(spec.employerIdColumn)
            if (idx != -1 && !remoteCursor.isNull(idx)) {
                // Employer ID column is always an FK to 'employers'
                val employerFk =
                    FKSpec(spec.employerIdColumn, "employers", "employerId", "employerName")
                recordLocalEmployerId = getLocalFkValue(
                    localDb, remoteDb, remoteCursor, employerFk, idx, idMap
                ).toLongOrNull()
            }
        }

        for (fk in spec.fks) {
            val fkIndex = remoteCursor.getColumnIndex(fk.fkColumn)
            if (fkIndex != -1) {
                handledColumns.add(fk.fkColumn)
                if (remoteCursor.isNull(fkIndex)) {
                    whereClauses.add("${fk.fkColumn} IS NULL")
                } else {
                    val localParentValue = getLocalFkValue(
                        localDb, remoteDb, remoteCursor, fk, fkIndex, idMap, recordLocalEmployerId
                    )
                    if (localParentValue == "-1") return null
                    whereClauses.add("${fk.fkColumn} = ?")
                    selectionArgs.add(localParentValue)
                }
            }
        }

        for (key in spec.keys) {
            if (handledColumns.contains(key)) continue
            val colIdx = remoteCursor.getColumnIndex(key)
            if (colIdx != -1) {
                val value = remoteCursor.getString(colIdx)?.trim() ?: ""
                whereClauses.add("TRIM($key) = ?")
                selectionArgs.add(value)
            }
        }

        if (whereClauses.isEmpty()) return null
        return Pair(whereClauses.joinToString(" AND "), selectionArgs.toTypedArray())
    }

    private fun getNameFromTable(
        db: SQLiteDatabase,
        table: String,
        pk: String,
        nameCol: String,
        id: Long
    ): String {
        return try {
            val cursor =
                db.rawQuery("SELECT $nameCol FROM $table WHERE $pk = ?", arrayOf(id.toString()))
            var name = ""
            if (cursor.moveToFirst()) name = cursor.getString(0).trim()
            cursor.close()
            name
        } catch (e: Exception) {
            "unknown Exception: $e"
        }
    }

    private fun getIdByName(
        db: SQLiteDatabase,
        table: String,
        pk: String,
        nameCol: String,
        name: String,
        employerId: Long? = null
    ): Long {
        if (name.isBlank()) return -1L
        return try {
            val employerCol = getTables().find { it.tableName == table }?.employerIdColumn
            val query = if (employerId != null && employerCol != null) {
                "SELECT $pk FROM $table WHERE TRIM($nameCol) = ? AND $employerCol = ?"
            } else {
                "SELECT $pk FROM $table WHERE TRIM($nameCol) = ?"
            }
            val args = if (employerId != null && employerCol != null) {
                arrayOf(name, employerId.toString())
            } else {
                arrayOf(name)
            }
            val cursor = db.rawQuery(query, args)
            var id = -1L
            if (cursor.moveToFirst()) id = cursor.getLong(0)
            cursor.close()
            id
        } catch (e: Exception) {
            -1L
        }
    }

    private fun getTables() = listOf(
        TableSpec(
            "taxTypes",
            listOf("taxType"),
            pkColumn = "taxTypeId",
            isDeletedColumn = "ttIsDeleted",
            updateTimeColumn = "ttUpdateTime"
        ),
        TableSpec(
            "employers",
            listOf("employerName"),
            pkColumn = "employerId",
            isDeletedColumn = "employerIsDeleted",
            updateTimeColumn = "employerUpdateTime"
        ),
        TableSpec(
            "areas",
            listOf("areaName"),
            pkColumn = "areaId",
            isDeletedColumn = "areaIsDeleted",
            updateTimeColumn = "areaUpdateTime"
        ),
        TableSpec(
            "workPerformed",
            listOf("wpDescription"),
            pkColumn = "workPerformedId",
            isDeletedColumn = "wpIsDeleted",
            updateTimeColumn = "wpUpdateTime"
        ),
        TableSpec(
            "jobSpecs",
            listOf("jsName"),
            pkColumn = "jobSpecId",
            isDeletedColumn = "jsIsDeleted",
            updateTimeColumn = "jsUpdateTime"
        ),
        TableSpec(
            "materials",
            listOf("mName"),
            pkColumn = "materialId",
            isDeletedColumn = "mIsDeleted",
            updateTimeColumn = "mUpdateTime"
        ),
        TableSpec(
            "taxEffectiveDates",
            listOf("tdEffectiveDate"),
            pkColumn = null, // PK is string
            isDeletedColumn = "tdIsDeleted",
            updateTimeColumn = "tdUpdateTime"
        ),

        TableSpec(
            "workExtraTypes",
            listOf("wetName", "wetEmployerId"),
            listOf(FKSpec("wetEmployerId", "employers", "employerId", "employerName")),
            pkColumn = "workExtraTypeId",
            isDeletedColumn = "wetIsDeleted",
            updateTimeColumn = "wetUpdateTime",
            employerIdColumn = "wetEmployerId"
        ),
        TableSpec(
            "payPeriods",
            listOf("ppCutoffDate", "ppEmployerId"),
            listOf(FKSpec("ppEmployerId", "employers", "employerId", "employerName")),
            pkColumn = "payPeriodId",
            isDeletedColumn = "ppIsDeleted",
            updateTimeColumn = "ppUpdateTime",
            employerIdColumn = "ppEmployerId"
        ),
        TableSpec(
            "workDates",
            listOf("wdDate", "wdEmployerId", "wdCutoffDate"),
            listOf(
                FKSpec("wdEmployerId", "employers", "employerId", "employerName"),
                FKSpec("wdPayPeriodId", "payPeriods", "payPeriodId", "ppCutoffDate", true)
            ),
            pkColumn = "workDateId",
            isDeletedColumn = "wdIsDeleted",
            updateTimeColumn = "wdUpdateTime",
            employerIdColumn = "wdEmployerId"
        ),
        TableSpec(
            "workOrders",
            listOf("woNumber", "woEmployerId"),
            listOf(FKSpec("woEmployerId", "employers", "employerId", "employerName")),
            pkColumn = "workOrderId",
            isDeletedColumn = "woDeleted",
            updateTimeColumn = "woUpdateTime",
            employerIdColumn = "woEmployerId"
        ),
        TableSpec(
            "workPerformedMerged",
            listOf("wpmMasterId", "wpmChildId"),
            listOf(
                FKSpec("wpmMasterId", "workPerformed", "workPerformedId", "wpDescription"),
                FKSpec("wpmChildId", "workPerformed", "workPerformedId", "wpDescription")
            ),
            pkColumn = "workPerformedMergeId",
            isDeletedColumn = "wpmIsDeleted",
            updateTimeColumn = "wpmUpdateTime"
        ),
        TableSpec(
            "materialMerged",
            listOf("mmMasterId", "mmChildId"),
            listOf(
                FKSpec("mmMasterId", "materials", "materialId", "mName"),
                FKSpec("mmChildId", "materials", "materialId", "mName")
            ),
            pkColumn = "materialMergeId",
            isDeletedColumn = "mmIsDeleted",
            updateTimeColumn = "mmUpdateTime"
        ),
        TableSpec(
            "jobSpecMerged",
            listOf("jsmMasterId", "jsmChildId"),
            listOf(
                FKSpec("jsmMasterId", "jobSpecs", "jobSpecId", "jsName"),
                FKSpec("jsmChildId", "jobSpecs", "jobSpecId", "jsName")
            ),
            pkColumn = "jobSpecMergedId",
            isDeletedColumn = "jsmIsDeleted",
            updateTimeColumn = "jsmUpdateTime"
        ),
        TableSpec(
            "employerTaxTypes",
            listOf("etrEmployerId", "etrTaxType"),
            listOf(
                FKSpec("etrEmployerId", "employers", "employerId", "employerName"),
                FKSpec("etrTaxType", "taxTypes", "taxType", "taxType")
            ),
            isDeletedColumn = "etrIsDeleted",
            updateTimeColumn = "etrUpdateTime",
            employerIdColumn = "etrEmployerId"
        ),
        TableSpec(
            "workTaxRules",
            listOf("wtType", "wtLevel", "wtEffectiveDate"),
            listOf(
                FKSpec("wtType", "taxTypes", "taxType", "taxType"),
                FKSpec("wtEffectiveDate", "taxEffectiveDates", "tdEffectiveDate", "tdEffectiveDate")
            ),
            pkColumn = "workTaxRuleId",
            isDeletedColumn = "wtIsDeleted",
            updateTimeColumn = "wtUpdateTime"
        ),
        TableSpec(
            "employerPayRates",
            listOf("eprEmployerId", "eprEffectiveDate"),
            listOf(FKSpec("eprEmployerId", "employers", "employerId", "employerName")),
            pkColumn = "employerPayRateId",
            isDeletedColumn = "eprIsDeleted",
            updateTimeColumn = "eprUpdateTime",
            employerIdColumn = "eprEmployerId"
        ),
        TableSpec(
            "workExtrasDefinitions",
            listOf("weEmployerId", "weExtraTypeId", "weEffectiveDate"),
            listOf(
                FKSpec("weEmployerId", "employers", "employerId", "employerName"),
                FKSpec("weExtraTypeId", "workExtraTypes", "workExtraTypeId", "wetName", true)
            ),
            pkColumn = "workExtraDefId",
            isDeletedColumn = "weIsDeleted",
            updateTimeColumn = "weUpdateTime",
            employerIdColumn = "weEmployerId"
        ),

        TableSpec(
            "workOrderHistory",
            listOf("woHistoryWorkOrderId", "woHistoryWorkDateId"),
            listOf(
                FKSpec("woHistoryWorkOrderId", "workOrders", "workOrderId", "woNumber", true),
                FKSpec("woHistoryWorkDateId", "workDates", "workDateId", "wdDate", true)
            ),
            pkColumn = "woHistoryId",
            isDeletedColumn = "woHistoryDeleted",
            updateTimeColumn = "woHistoryUpdateTime"
        ),
        TableSpec(
            "workOrderJobSpecs",
            listOf("wojsWorkOrderId", "wojsJobSpecId", "wojsAreaId"),
            listOf(
                FKSpec("wojsWorkOrderId", "workOrders", "workOrderId", "woNumber", true),
                FKSpec("wojsJobSpecId", "jobSpecs", "jobSpecId", "jsName"),
                FKSpec("wojsAreaId", "areas", "areaId", "areaName")
            ),
            pkColumn = "workOrderJobSpecId",
            isDeletedColumn = "wojsIsDeleted",
            updateTimeColumn = "wojsUpdateTime"
        ),
        TableSpec(
            "workDateExtras",
            listOf("wdeWorkDateId", "wdeName"),
            listOf(
                FKSpec("wdeWorkDateId", "workDates", "workDateId", "wdDate", true),
                FKSpec("wdeExtraTypeId", "workExtraTypes", "workExtraTypeId", "wetName", true)
            ),
            pkColumn = "workDateExtraId",
            isDeletedColumn = "wdeIsDeleted",
            updateTimeColumn = "wdeUpdateTime"
        ),
        TableSpec(
            "workPayPeriodExtras",
            listOf("ppePayPeriodId", "ppeName"),
            listOf(
                FKSpec("ppePayPeriodId", "payPeriods", "payPeriodId", "ppCutoffDate", true),
                FKSpec("ppeExtraTypeId", "workExtraTypes", "workExtraTypeId", "wetName", true)
            ),
            pkColumn = "workPayPeriodExtraId",
            isDeletedColumn = "ppeIsDeleted",
            updateTimeColumn = "ppeUpdateTime"
        ),
        TableSpec(
            "workPayPeriodTax",
            listOf("wppCutoffDate", "wppEmployerId", "wppTaxType"),
            listOf(
                FKSpec("wppEmployerId", "employers", "employerId", "employerName"),
                FKSpec("wppTaxType", "taxTypes", "taxType", "taxType")
            ),
            pkColumn = null, // PK is composite
            isDeletedColumn = "wppIsDeleted",
            updateTimeColumn = "wppUpdateTime",
            employerIdColumn = "wppEmployerId"
        ),

        TableSpec(
            "workOrderHistoryMaterials",
            listOf("wohmHistoryId", "wohmMaterialId"),
            listOf(
                FKSpec("wohmHistoryId", "workOrderHistory", "woHistoryId", "woHistoryId"),
                FKSpec("wohmMaterialId", "materials", "materialId", "mName")
            ),
            pkColumn = "workOrderHistoryMaterialId",
            isDeletedColumn = "wohmIsDeleted",
            updateTimeColumn = "wohmUpdateTime"
        ),
        TableSpec(
            "workOrderHistoryWorkPerformed",
            listOf("wowpHistoryId", "wowpWorkPerformedId", "wowpAreaId"),
            listOf(
                FKSpec("wowpHistoryId", "workOrderHistory", "woHistoryId", "woHistoryId"),
                FKSpec("wowpWorkPerformedId", "workPerformed", "workPerformedId", "wpDescription"),
                FKSpec("wowpAreaId", "areas", "areaId", "areaName")
            ),
            pkColumn = "workOrderHistoryWorkPerformedId",
            isDeletedColumn = "wowpIsDeleted",
            updateTimeColumn = "wowpUpdateTime"
        ),
        TableSpec(
            "workOrderHistoryTimeWorked",
            listOf("wohtDateId", "wohtStartTime"),
            listOf(
                FKSpec("wohtHistoryId", "workOrderHistory", "woHistoryId", "woHistoryId"),
                FKSpec("wohtDateId", "workDates", "workDateId", "wdDate", true)
            ),
            pkColumn = "woHistoryTimeWorkedId",
            isDeletedColumn = "wohtIsDeleted",
            updateTimeColumn = "wohtUpdateTime"
        )
    )

    data class TableSpec(
        val tableName: String,
        val keys: List<String>,
        val fks: List<FKSpec> = emptyList(),
        val isDeletedColumn: String? = null,
        val updateTimeColumn: String? = null,
        val pkColumn: String? = null,
        val employerIdColumn: String? = null // Column that links to an employer
    )

    data class FKSpec(
        val fkColumn: String,
        val parentTable: String,
        val parentPk: String,
        val parentNaturalKey: String,
        val dependsOnEmployer: Boolean = false // If true, use employerIdColumn for lookup
    )
}