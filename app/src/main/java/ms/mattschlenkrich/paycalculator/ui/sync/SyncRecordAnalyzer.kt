package ms.mattschlenkrich.paycalculator.ui.sync

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import ms.mattschlenkrich.paycalculator.common.SQLITE_TIME
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

enum class RecordStatus { NEW, UPDATED, EXISTS }

fun isTableExists(db: SQLiteDatabase, tableName: String): Boolean {
    val cursor = db.rawQuery(
        "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
        arrayOf(tableName)
    )
    val exists = cursor.count > 0
    cursor.close()
    return exists
}

fun getLookbackTime(localDb: SQLiteDatabase): String {
    val formatter = SimpleDateFormat(SQLITE_TIME, Locale.CANADA)
    formatter.timeZone = TimeZone.getTimeZone("UTC")

    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.add(Calendar.DAY_OF_YEAR, -28) // 4-week safety window
    val hardLimit = formatter.format(calendar.time)

    val earliestLastSync = try {
        val query =
            "SELECT MIN(lastSync) FROM (SELECT MAX(syncTime) as lastSync FROM syncHistory WHERE syncStatus = 'Success' AND syncTime > '$hardLimit' GROUP BY syncDeviceId)"
        val cursor = localDb.rawQuery(query, null)
        var time: String? = null
        if (cursor.moveToFirst()) time = cursor.getString(0)
        cursor.close()
        time
    } catch (e: Exception) {
        null
    }

    if (earliestLastSync == null) return "1970-01-01 00:00:00"

    // Add 2-hour safety buffer to the earliest sync found
    val bufferedTime = try {
        val syncCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        syncCal.time = formatter.parse(earliestLastSync)!!
        syncCal.add(Calendar.HOUR_OF_DAY, -2)
        formatter.format(syncCal.time)
    } catch (e: Exception) {
        hardLimit
    }

    return if (bufferedTime < hardLimit) hardLimit else bufferedTime
}

fun checkRecordStatus(
    localDb: SQLiteDatabase,
    remoteDb: SQLiteDatabase,
    remoteCursor: Cursor,
    spec: TableSpec
): RecordStatus {
    return checkRecordStatusWithId(localDb, remoteDb, remoteCursor, spec, emptyMap()).first
}

fun checkRecordStatusWithId(
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

            status =
                if (remoteUpdateTime != null && (localUpdateTime == null || remoteUpdateTime > localUpdateTime)) {
                    RecordStatus.UPDATED
                } else if (remoteUpdateTime != null && remoteUpdateTime == localUpdateTime) {
                    if (isDataDifferent(localCursor, remoteCursor, spec)) {
                        RecordStatus.UPDATED
                    } else {
                        RecordStatus.EXISTS
                    }
                } else {
                    RecordStatus.EXISTS
                }
        } else {
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

fun isDataDifferent(
    localCursor: Cursor,
    remoteCursor: Cursor,
    spec: TableSpec
): Boolean {
    for (i in 0 until remoteCursor.columnCount) {
        val colName = remoteCursor.getColumnName(i)
        if (colName == spec.pkColumn || colName == spec.updateTimeColumn) continue

        val localIdx = localCursor.getColumnIndex(colName)
        if (localIdx == -1) continue

        if (remoteCursor.isNull(i) != localCursor.isNull(localIdx)) return true
        if (remoteCursor.isNull(i)) continue

        val type = remoteCursor.getType(i)
        if (type != localCursor.getType(localIdx)) return true

        val isDifferent = when (type) {
            Cursor.FIELD_TYPE_INTEGER -> remoteCursor.getLong(i) != localCursor.getLong(localIdx)
            Cursor.FIELD_TYPE_FLOAT -> remoteCursor.getDouble(i) != localCursor.getDouble(localIdx)
            Cursor.FIELD_TYPE_STRING -> remoteCursor.getString(i) != localCursor.getString(localIdx)
            Cursor.FIELD_TYPE_BLOB -> !remoteCursor.getBlob(i)
                .contentEquals(localCursor.getBlob(localIdx))

            else -> false
        }
        if (isDifferent) return true
    }
    return false
}

fun buildWhereClause(
    localDb: SQLiteDatabase,
    remoteDb: SQLiteDatabase,
    remoteCursor: Cursor,
    spec: TableSpec,
    idMap: Map<String, Map<Long, Long>>
): Pair<String, Array<String>>? {
    val whereClauses = mutableListOf<String>()
    val selectionArgs = mutableListOf<String>()
    val handledColumns = mutableSetOf<String>()

    var recordLocalEmployerId: Long? = null
    if (spec.employerIdColumn != null) {
        val idx = remoteCursor.getColumnIndex(spec.employerIdColumn)
        if (idx != -1 && !remoteCursor.isNull(idx)) {
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

fun getNameFromTable(
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

fun getIdByName(
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
    } catch (_: Exception) {
        -1L
    }
}

fun findNewAndUpdatedRecords(
    localDb: SQLiteDatabase,
    remoteDb: SQLiteDatabase,
    spec: TableSpec,
    lastSyncTime: String? = null
): Pair<List<String>, List<String>> {
    val newItems = mutableListOf<String>()
    val updatedItems = mutableListOf<String>()

    val query = if (lastSyncTime != null && spec.updateTimeColumn != null) {
        "SELECT * FROM ${spec.tableName} WHERE ${spec.updateTimeColumn} > '$lastSyncTime'"
    } else {
        "SELECT * FROM ${spec.tableName}"
    }

    try {
        val cursor = remoteDb.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val status = checkRecordStatus(localDb, remoteDb, cursor, spec)
                val displayName = try {
                    if (spec.keys.isNotEmpty()) {
                        val colIdx = cursor.getColumnIndex(spec.keys[0])
                        if (colIdx != -1) cursor.getString(colIdx).trim() else "Unknown"
                    } else if (spec.fks.isNotEmpty()) {
                        val fk = spec.fks[0]
                        val colIdx = cursor.getColumnIndex(fk.fkColumn)
                        if (colIdx != -1) {
                            getNameFromTable(
                                remoteDb,
                                fk.parentTable,
                                fk.parentPk,
                                fk.parentNaturalKey,
                                cursor.getLong(colIdx)
                            )
                        } else "Unknown"
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
        Log.w("SyncRecordAnalyzer", "Table ${spec.tableName} query failed: ${e.message}")
    }
    return Pair(newItems, updatedItems)
}