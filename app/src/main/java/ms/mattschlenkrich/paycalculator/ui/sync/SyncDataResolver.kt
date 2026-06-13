package ms.mattschlenkrich.paycalculator.ui.sync

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

fun getContentValues(
    localDb: SQLiteDatabase,
    remoteDb: SQLiteDatabase,
    cursor: Cursor,
    spec: TableSpec,
    idMap: Map<String, Map<Long, Long>>,
    localId: Long = -1L
): ContentValues {
    val values = ContentValues()

    var recordLocalEmployerId: Long? = null
    if (spec.employerIdColumn != null) {
        val idx = cursor.getColumnIndex(spec.employerIdColumn)
        if (idx != -1 && !cursor.isNull(idx)) {
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

fun getLocalFkValue(
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