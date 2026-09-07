package ms.mattschlenkrich.paycalculator.ui.sync

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.TABLE_EMPLOYERS
import ms.mattschlenkrich.paycalculator.common.TABLE_EMPLOYER_PAY_RATES
import ms.mattschlenkrich.paycalculator.common.TABLE_EMPLOYER_TAX_TYPES
import ms.mattschlenkrich.paycalculator.common.TABLE_PAY_PERIODS
import ms.mattschlenkrich.paycalculator.common.TABLE_SYNC_HISTORY
import ms.mattschlenkrich.paycalculator.common.TABLE_TAX_EFFECTIVE_DATES
import ms.mattschlenkrich.paycalculator.common.TABLE_TAX_TYPES
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_DATES
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_DATE_EXTRAS
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_EXTRAS_DEFINITIONS
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_EXTRA_TYPES
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_PAY_PERIOD_EXTRAS
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_TAX_RULES
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.Areas
import ms.mattschlenkrich.paycalculator.data.entity.EmployerPayRates
import ms.mattschlenkrich.paycalculator.data.entity.EmployerTaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec
import ms.mattschlenkrich.paycalculator.data.entity.JobSpecMerged
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.entity.MaterialMerged
import ms.mattschlenkrich.paycalculator.data.entity.PayPeriods
import ms.mattschlenkrich.paycalculator.data.entity.SyncHistory
import ms.mattschlenkrich.paycalculator.data.entity.TaxEffectiveDates
import ms.mattschlenkrich.paycalculator.data.entity.TaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtrasDefinitions
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryExpense
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.data.entity.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformedMerged
import ms.mattschlenkrich.paycalculator.data.entity.WorkTaxRules

private const val TAG = "DatabaseSyncHelper"

const val TABLE_WORK_ORDERS = "workOrders"
const val TABLE_WORK_ORDER_HISTORY = "workOrderHistory"
const val TABLE_WORK_PERFORMED = "workPerformed"
const val TABLE_JOB_SPECS = "jobSpecs"
const val TABLE_WORK_ORDER_HISTORY_WORK_PERFORMED = "workOrderHistoryWorkPerformed"
const val TABLE_WORK_ORDER_HISTORY_JOB_SPECS = "workOrderJobSpecs"
const val TABLE_MATERIALS = "materials"
const val TABLE_WORK_ORDER_HISTORY_MATERIALS = "workOrderHistoryMaterials"
const val TABLE_AREAS = "areas"
const val TABLE_JOB_SPEC_MERGED = "jobSpecMerged"
const val TABLE_MATERIAL_MERGED = "materialMerged"
const val TABLE_WORK_PERFORMED_MERGED = "workPerformedMerged"
const val TABLE_WORK_ORDER_HISTORY_TIME_WORKED = "workOrderHistoryTimeWorked"
const val TABLE_WORK_ORDER_HISTORY_EXPENSE = "workOrderHistoryExpense-*-"

class DatabaseSyncHelper(
    private val appDb: PayDatabase,
    private val df: DateFunctions,
    private val deviceId: Long,
    private val onConflict: suspend (ConflictInfo) -> ConflictChoice,
    private val onSyncError: (String) -> Unit,
    private val isRestore: Boolean = false,
) {

    private fun getStringSafe(cursor: Cursor, columnName: String): String {
        return try {
            val index = cursor.getColumnIndexOrThrow(columnName)
            if (cursor.isNull(index)) "" else cursor.getString(index)
        } catch (_: Exception) {
            ""
        }
    }

    private fun getLongSafe(cursor: Cursor, columnName: String): Long {
        return try {
            val index = cursor.getColumnIndexOrThrow(columnName)
            if (cursor.isNull(index)) 0L else cursor.getLong(index)
        } catch (_: Exception) {
            0L
        }
    }

    private fun getDoubleSafe(cursor: Cursor, columnName: String): Double {
        return try {
            val index = cursor.getColumnIndexOrThrow(columnName)
            if (cursor.isNull(index)) 0.0 else cursor.getDouble(index)
        } catch (_: Exception) {
            0.0
        }
    }

    private fun getIntSafe(cursor: Cursor, columnName: String): Int {
        return try {
            val index = cursor.getColumnIndexOrThrow(columnName)
            if (cursor.isNull(index)) 0 else cursor.getInt(index)
        } catch (_: Exception) {
            0
        }
    }

    private fun getBooleanSafe(cursor: Cursor, columnName: String): Boolean {
        return getIntSafe(cursor, columnName) != 0
    }

    suspend fun <T> syncTable(
        backupDb: SQLiteDatabase,
        tableName: String,
        mapCursorToItem: (Cursor) -> T,
        getExistingById: suspend (T) -> T?,
        getExistingByName: (suspend (T) -> T?)? = null,
        getUpdateTime: (T) -> String,
        getName: ((T) -> String)? = null,
        getId: ((T) -> Long)? = null,
        insert: suspend (T) -> Unit,
        update: suspend (T) -> Unit,
        rename: (suspend (Long, String, String) -> Unit)? = null,
        copyWithName: ((T, String) -> T)? = null,
    ): Pair<Int, Int> {
        var inserts = 0
        var updates = 0

        Log.d(TAG, "Syncing table: $tableName")

        backupDb.query(tableName, null, null, null, null, null, null).use { cursor ->
            while (cursor.moveToNext()) {
                val backupItem = try {
                    mapCursorToItem(cursor)
                } catch (_: Exception) {
                    onSyncError("Skipped a record in $tableName due to data error.")
                    continue
                }
                val existingById = getExistingById(backupItem)
                val backupTime = getUpdateTime(backupItem)

                if (existingById == null) {
                    val existingByName =
                        if (isRestore) null else getExistingByName?.invoke(backupItem)
                    if ((existingByName != null) && (getName != null) && (getId != null)) {
                        val localName = getName(existingByName)
                        val localId = getId(existingByName)
                        val localTime = getUpdateTime(existingByName)

                        val choice = onConflict(
                            ConflictInfo(
                                tableName,
                                localName,
                                localId,
                                localTime,
                                getId(backupItem),
                                backupTime,
                            )
                        )

                        when (choice) {
                            ConflictChoice.KEEP_LOCAL -> {
                                val newBackupName =
                                    "${getName(backupItem)}_DRIVE_${getId(backupItem)}"
                                val renamedItem =
                                    copyWithName?.invoke(backupItem, newBackupName)
                                        ?: backupItem
                                try {
                                    insert(renamedItem)
                                    inserts++
                                } catch (e: Exception) {
                                    Log.e(TAG, "Conflict inserting $tableName", e)
                                }
                            }

                            ConflictChoice.KEEP_DRIVE -> {
                                val newLocalName = "${localName}_LOCAL_$localId"
                                try {
                                    rename?.invoke(
                                        localId,
                                        newLocalName,
                                        df.getCurrentUTCTimeAsString()
                                    )
                                    insert(backupItem)
                                    inserts++
                                } catch (e: Exception) {
                                    Log.e(TAG, "Conflict inserting $tableName", e)
                                }
                            }
                        }
                    } else {
                        try {
                            insert(backupItem)
                            inserts++
                        } catch (e: Exception) {
                            Log.e(TAG, "Error inserting $tableName", e)
                        }
                    }
                } else {
                    val localTime = getUpdateTime(existingById)
                    if (isRestore || (backupTime > localTime)) {
                        update(backupItem)
                        updates++
                    }
                }
            }
        }
        return Pair(inserts, updates)
    }

    suspend fun syncEmployers(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_EMPLOYERS,
            mapCursorToItem = { cursor ->
                Employers(
                    employerId = getLongSafe(cursor, "employerId"),
                    employerName = getStringSafe(cursor, "employerName"),
                    payFrequency = getStringSafe(cursor, "payFrequency"),
                    startDate = getStringSafe(cursor, "startDate"),
                    dayOfWeek = getStringSafe(cursor, "dayOfWeek"),
                    cutoffDaysBefore = getIntSafe(cursor, "cutoffDaysBefore"),
                    midMonthlyDate = getIntSafe(cursor, "midMonthlyDate"),
                    mainMonthlyDate = getIntSafe(cursor, "mainMonthlyDate"),
                    employerIsDeleted = getBooleanSafe(cursor, "employerIsDeleted"),
                    employerUpdateTime = getStringSafe(cursor, "employerUpdateTime")
                )
            },
            getExistingById = { appDb.getEmployerDao().getEmployerSync(it.employerId) },
            getExistingByName = {
                appDb.getEmployerDao().findEmployerByNameAnySync(it.employerName)
            },
            getUpdateTime = { it.employerUpdateTime },
            getName = { it.employerName },
            getId = { it.employerId },
            insert = { appDb.getEmployerDao().insertEmployer(it) },
            update = { appDb.getEmployerDao().updateEmployer(it) },
            rename = { id, name, time -> appDb.getEmployerDao().renameEmployer(id, name, time) },
            copyWithName = { item, name -> item.copy(employerName = name) },
        )
    }

    suspend fun syncTaxTypes(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_TAX_TYPES,
            mapCursorToItem = { cursor ->
                TaxTypes(
                    taxTypeId = getLongSafe(cursor, "taxTypeId"),
                    taxType = getStringSafe(cursor, "taxType"),
                    ttBasedOn = getIntSafe(cursor, "ttBasedOn"),
                    ttIsDeleted = getBooleanSafe(cursor, "ttIsDeleted"),
                    ttUpdateTime = getStringSafe(cursor, "ttUpdateTime")
                )
            },
            getExistingById = { appDb.getWorkTaxDao().getTaxTypeByIdSync(it.taxTypeId) },
            getExistingByName = { appDb.getWorkTaxDao().getTaxTypeAnySync(it.taxType) },
            getUpdateTime = { it.ttUpdateTime },
            getName = { it.taxType },
            getId = { it.taxTypeId },
            insert = { appDb.getWorkTaxDao().insertTaxType(it) },
            update = { appDb.getWorkTaxDao().updateWorkTaxType(it) },
            rename = { id, name, time -> appDb.getWorkTaxDao().renameTaxType(id, name, time) },
            copyWithName = { item, name -> item.copy(taxType = name) },
        )
    }

    suspend fun syncTaxEffectiveDates(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_TAX_EFFECTIVE_DATES,
            mapCursorToItem = { cursor ->
                TaxEffectiveDates(
                    tdEffectiveDate = getStringSafe(cursor, "tdEffectiveDate"),
                    tdEffectiveDateId = getLongSafe(cursor, "tdEffectiveDateId"),
                    tdIsDeleted = getBooleanSafe(cursor, "tdIsDeleted"),
                    tdUpdateTime = getStringSafe(cursor, "tdUpdateTime")
                )
            },
            getExistingById = { appDb.getWorkTaxDao().getEffectiveDateSync(it.tdEffectiveDate) },
            getUpdateTime = { it.tdUpdateTime },
            insert = { appDb.getWorkTaxDao().insertEffectiveDate(it) },
            update = { /* No update */ },
        )
    }

    suspend fun syncWorkTaxRules(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_WORK_TAX_RULES,
            mapCursorToItem = { cursor ->
                WorkTaxRules(
                    workTaxRuleId = getLongSafe(cursor, "workTaxRuleId"),
                    wtType = getStringSafe(cursor, "wtType"),
                    wtLevel = getIntSafe(cursor, "wtLevel"),
                    wtEffectiveDate = getStringSafe(cursor, "wtEffectiveDate"),
                    wtPercent = getDoubleSafe(cursor, "wtPercent"),
                    wtHasExemption = getBooleanSafe(cursor, "wtHasExemption"),
                    wtExemptionAmount = getDoubleSafe(cursor, "wtExemptionAmount"),
                    wtHasBracket = getBooleanSafe(cursor, "wtHasBracket"),
                    wtBracketAmount = getDoubleSafe(cursor, "wtBracketAmount"),
                    wtIsDeleted = getBooleanSafe(cursor, "wtIsDeleted"),
                    wtUpdateTime = getStringSafe(cursor, "wtUpdateTime")
                )
            },
            getExistingById = { appDb.getWorkTaxDao().getWorkTaxRuleSync(it.workTaxRuleId) },
            getUpdateTime = { it.wtUpdateTime },
            insert = { appDb.getWorkTaxDao().insertTaxRule(it) },
            update = { appDb.getWorkTaxDao().updateTaxRule(it) },
        )
    }

    suspend fun syncEmployerTaxTypes(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_EMPLOYER_TAX_TYPES,
            mapCursorToItem = { cursor ->
                EmployerTaxTypes(
                    etrEmployerId = getLongSafe(cursor, "etrEmployerId"),
                    etrTaxType = getStringSafe(cursor, "etrTaxType"),
                    etrInclude = getBooleanSafe(cursor, "etrInclude"),
                    etrIsDeleted = getBooleanSafe(cursor, "etrIsDeleted"),
                    etrUpdateTime = getStringSafe(cursor, "etrUpdateTime")
                )
            },
            getExistingById = {
                appDb.getWorkTaxDao().getEmployerTaxTypeSync(it.etrEmployerId, it.etrTaxType)
            },
            getUpdateTime = { it.etrUpdateTime },
            insert = { appDb.getWorkTaxDao().insertEmployerTaxType(it) },
            update = { appDb.getWorkTaxDao().updateEmployerTaxType(it) },
        )
    }

    suspend fun syncEmployerPayRates(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_EMPLOYER_PAY_RATES,
            mapCursorToItem = { cursor ->
                EmployerPayRates(
                    employerPayRateId = getLongSafe(cursor, "employerPayRateId"),
                    eprEmployerId = getLongSafe(cursor, "eprEmployerId"),
                    eprEffectiveDate = getStringSafe(cursor, "eprEffectiveDate"),
                    eprPerPeriod = getIntSafe(cursor, "eprPerPeriod"),
                    eprPayRate = getDoubleSafe(cursor, "eprPayRate"),
                    eprIsDeleted = getBooleanSafe(cursor, "eprIsDeleted"),
                    eprUpdateTime = getStringSafe(cursor, "eprUpdateTime")
                )
            },
            getExistingById = { /* null */ null },
            getUpdateTime = { it.eprUpdateTime },
            insert = { appDb.getEmployerDao().insertPayRate(it) },
            update = { appDb.getEmployerDao().updatePayRate(it) },
        )
    }

    suspend fun syncWorkExtraTypes(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_WORK_EXTRA_TYPES,
            mapCursorToItem = { cursor ->
                WorkExtraTypes(
                    workExtraTypeId = getLongSafe(cursor, "workExtraTypeId"),
                    wetName = getStringSafe(cursor, "wetName"),
                    wetEmployerId = getLongSafe(cursor, "wetEmployerId"),
                    wetAppliesTo = getIntSafe(cursor, "wetAppliesTo"),
                    wetAttachTo = getIntSafe(cursor, "wetAttachTo"),
                    wetIsCredit = getBooleanSafe(cursor, "wetIsCredit"),
                    wetIsDefault = getBooleanSafe(cursor, "wetIsDefault"),
                    wetIsDeleted = getBooleanSafe(cursor, "wetIsDeleted"),
                    wetUpdateTime = getStringSafe(cursor, "wetUpdateTime")
                )
            },
            getExistingById = { appDb.getWorkExtraDao().getExtraTypeSync(it.workExtraTypeId) },
            getUpdateTime = { it.wetUpdateTime },
            getName = { it.wetName },
            getId = { it.workExtraTypeId },
            insert = { appDb.getWorkExtraDao().insertWorkExtraType(it) },
            update = { appDb.getWorkExtraDao().updateWorkExtraType(it) },
            rename = { id, name, time ->
                appDb.getWorkExtraDao().renameWorkExtraType(id, name, time)
            },
            copyWithName = { item, name -> item.copy(wetName = name) },
        )
    }

    suspend fun syncWorkExtrasDefinitions(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_WORK_EXTRAS_DEFINITIONS,
            mapCursorToItem = { cursor ->
                WorkExtrasDefinitions(
                    workExtraDefId = getLongSafe(cursor, "workExtraDefId"),
                    weEmployerId = getLongSafe(cursor, "weEmployerId"),
                    weExtraTypeId = getLongSafe(cursor, "weExtraTypeId"),
                    weValue = getDoubleSafe(cursor, "weValue"),
                    weIsFixed = getBooleanSafe(cursor, "weIsFixed"),
                    weEffectiveDate = getStringSafe(cursor, "weEffectiveDate"),
                    weIsDeleted = getBooleanSafe(cursor, "weIsDeleted"),
                    weUpdateTime = getStringSafe(cursor, "weUpdateTime")
                )
            },
            getExistingById = { appDb.getWorkExtraDao().getExtraDefinitionSync(it.workExtraDefId) },
            getUpdateTime = { it.weUpdateTime },
            insert = { appDb.getWorkExtraDao().insertWorkExtraDefinition(it) },
            update = { appDb.getWorkExtraDao().updateWorkExtraDefinition(it) },
        )
    }

    suspend fun syncPayPeriods(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_PAY_PERIODS,
            mapCursorToItem = { cursor ->
                PayPeriods(
                    payPeriodId = getLongSafe(cursor, "payPeriodId"),
                    ppCutoffDate = getStringSafe(cursor, "ppCutoffDate"),
                    ppEmployerId = getLongSafe(cursor, "ppEmployerId"),
                    ppIsDeleted = getBooleanSafe(cursor, "ppIsDeleted"),
                    ppUpdateTime = getStringSafe(cursor, "ppUpdateTime")
                )
            },
            getExistingById = {
                appDb.getPayDayDao().getPayPeriodAnySync(it.ppCutoffDate, it.ppEmployerId)
            },
            getUpdateTime = { it.ppUpdateTime },
            insert = { appDb.getPayDayDao().insertPayPeriod(it) },
            update = { appDb.getPayDayDao().updatePayPeriod(it) },
        )
    }

    suspend fun syncWorkDates(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_WORK_DATES,
            mapCursorToItem = { cursor ->
                WorkDates(
                    workDateId = getLongSafe(cursor, "workDateId"),
                    wdPayPeriodId = getLongSafe(cursor, "wdPayPeriodId"),
                    wdEmployerId = getLongSafe(cursor, "wdEmployerId"),
                    wdCutoffDate = getStringSafe(cursor, "wdCutoffDate"),
                    wdDate = getStringSafe(cursor, "wdDate"),
                    wdRegHours = getDoubleSafe(cursor, "wdRegHours"),
                    wdOtHours = getDoubleSafe(cursor, "wdOtHours"),
                    wdDblOtHours = getDoubleSafe(cursor, "wdDblOtHours"),
                    wdStatHours = getDoubleSafe(cursor, "wdStatHours"),
                    wdNote = getStringSafe(cursor, "wdNote"),
                    wdIsDeleted = getBooleanSafe(cursor, "wdIsDeleted"),
                    wdUpdateTime = getStringSafe(cursor, "wdUpdateTime")
                )
            },
            getExistingById = { appDb.getPayDayDao().getWorkDateByIdAnySync(it.workDateId) },
            getUpdateTime = { it.wdUpdateTime },
            insert = { appDb.getPayDayDao().insertWorkDate(it) },
            update = { appDb.getPayDayDao().updateWorkDate(it) },
        )
    }

    suspend fun syncWorkDateExtras(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_WORK_DATE_EXTRAS,
            mapCursorToItem = { cursor ->
                WorkDateExtras(
                    workDateExtraId = getLongSafe(cursor, "workDateExtraId"),
                    wdeWorkDateId = getLongSafe(cursor, "wdeWorkDateId"),
                    wdeExtraTypeId = if (cursor.isNull(cursor.getColumnIndexOrThrow("wdeExtraTypeId"))) null else cursor.getLong(
                        cursor.getColumnIndexOrThrow("wdeExtraTypeId")
                    ),
                    wdeName = getStringSafe(cursor, "wdeName"),
                    wdeAppliesTo = getIntSafe(cursor, "wdeAppliesTo"),
                    wdeAttachTo = getIntSafe(cursor, "wdeAttachTo"),
                    wdeValue = getDoubleSafe(cursor, "wdeValue"),
                    wdeIsFixed = getBooleanSafe(cursor, "wdeIsFixed"),
                    wdeIsCredit = getBooleanSafe(cursor, "wdeIsCredit"),
                    wdeIsDeleted = getBooleanSafe(cursor, "wdeIsDeleted"),
                    wdeUpdateTime = getStringSafe(cursor, "wdeUpdateTime")
                )
            },
            getExistingById = { appDb.getPayDayDao().getWorkDateExtraSync(it.workDateExtraId) },
            getUpdateTime = { it.wdeUpdateTime },
            insert = { appDb.getPayDayDao().insertWorkDateExtra(it) },
            update = { appDb.getPayDayDao().updateWorkDateExtra(it) },
        )
    }

    suspend fun syncWorkPayPeriodExtras(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_WORK_PAY_PERIOD_EXTRAS,
            mapCursorToItem = { cursor ->
                WorkPayPeriodExtras(
                    workPayPeriodExtraId = getLongSafe(cursor, "workPayPeriodExtraId"),
                    ppePayPeriodId = getLongSafe(cursor, "ppePayPeriodId"),
                    ppeExtraTypeId = if (cursor.isNull(cursor.getColumnIndexOrThrow("ppeExtraTypeId"))) null else cursor.getLong(
                        cursor.getColumnIndexOrThrow("ppeExtraTypeId")
                    ),
                    ppeName = getStringSafe(cursor, "ppeName"),
                    ppeAppliesTo = getIntSafe(cursor, "ppeAppliesTo"),
                    ppeAttachTo = getIntSafe(cursor, "ppeAttachTo"),
                    ppeValue = getDoubleSafe(cursor, "ppeValue"),
                    ppeIsFixed = getBooleanSafe(cursor, "ppeIsFixed"),
                    ppeIsCredit = getBooleanSafe(cursor, "ppeIsCredit"),
                    ppeIsDeleted = getBooleanSafe(cursor, "ppeIsDeleted"),
                    ppeUpdateTime = getStringSafe(cursor, "ppeUpdateTime")
                )
            },
            getExistingById = {
                appDb.getPayDayDao().getWorkPayPeriodExtraSync(it.workPayPeriodExtraId)
            },
            getUpdateTime = { it.ppeUpdateTime },
            insert = { appDb.getPayDayDao().insertPayPeriodExtra(it) },
            update = { appDb.getPayDayDao().updatePayPeriodExtra(it) },
        )
    }

    suspend fun syncWorkOrders(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_WORK_ORDERS,
            mapCursorToItem = { cursor ->
                WorkOrder(
                    workOrderId = getLongSafe(cursor, "workOrderId"),
                    woNumber = getStringSafe(cursor, "woNumber"),
                    woEmployerId = getLongSafe(cursor, "woEmployerId"),
                    woAddress = getStringSafe(cursor, "woAddress"),
                    woDescription = getStringSafe(cursor, "woDescription"),
                    woDeleted = getBooleanSafe(cursor, "woDeleted"),
                    woUpdateTime = getStringSafe(cursor, "woUpdateTime")
                )
            },
            getExistingById = { appDb.getWorkOrderDao().getWorkOrderByIdAnySync(it.workOrderId) },
            getExistingByName = {
                appDb.getWorkOrderDao().findWorkOrderAnySync(it.woNumber, it.woEmployerId)
            },
            getUpdateTime = { it.woUpdateTime },
            getName = { it.woNumber },
            getId = { it.workOrderId },
            insert = { appDb.getWorkOrderDao().insertWorkOrder(it) },
            update = { workOrder ->
                appDb.getWorkOrderDao().updateWorkOrder(
                    workOrder.workOrderId, workOrder.woNumber, workOrder.woEmployerId,
                    workOrder.woAddress, workOrder.woDescription, workOrder.woDeleted,
                    workOrder.woUpdateTime
                )
            },
        )
    }

    suspend fun syncWorkOrderHistory(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_WORK_ORDER_HISTORY,
            mapCursorToItem = { cursor ->
                WorkOrderHistory(
                    woHistoryId = getLongSafe(cursor, "woHistoryId"),
                    woHistoryWorkOrderId = getLongSafe(cursor, "woHistoryWorkOrderId"),
                    woHistoryWorkDateId = getLongSafe(cursor, "woHistoryWorkDateId"),
                    woHistoryRegHours = getDoubleSafe(cursor, "woHistoryRegHours"),
                    woHistoryOtHours = getDoubleSafe(cursor, "woHistoryOtHours"),
                    woHistoryDblOtHours = getDoubleSafe(cursor, "woHistoryDblOtHours"),
                    woHistoryNote = getStringSafe(cursor, "woHistoryNote"),
                    woHistoryDeleted = getBooleanSafe(cursor, "woHistoryDeleted"),
                    woHistoryUpdateTime = getStringSafe(cursor, "woHistoryUpdateTime")
                )
            },
            getExistingById = {
                appDb.getWorkOrderDao().getWorkOrderHistoryByIdAnySync(it.woHistoryId)
            },
            getUpdateTime = { it.woHistoryUpdateTime },
            insert = { appDb.getWorkOrderDao().insertWorkOrderHistory(it) },
            update = { appDb.getWorkOrderDao().updateWorkOrderHistory(it) },
        )
    }

    suspend fun syncWorkPerformed(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_WORK_PERFORMED,
            mapCursorToItem = { cursor ->
                WorkPerformed(
                    workPerformedId = getLongSafe(cursor, "workPerformedId"),
                    wpDescription = getStringSafe(cursor, "wpDescription"),
                    wpIsDeleted = getBooleanSafe(cursor, "wpIsDeleted"),
                    wpUpdateTime = getStringSafe(cursor, "wpUpdateTime")
                )
            },
            getExistingById = {
                appDb.getWorkPerformedDao().getWorkPerformedByIdSync(it.workPerformedId)
            },
            getExistingByName = {
                appDb.getWorkPerformedDao().getWorkPerformedAnySync(it.wpDescription)
            },
            getUpdateTime = { it.wpUpdateTime },
            getName = { it.wpDescription },
            getId = { it.workPerformedId },
            insert = { appDb.getWorkPerformedDao().insertWorkPerformed(it) },
            update = { appDb.getWorkPerformedDao().updateWorkPerformed(it) },
            rename = { id, name, time ->
                appDb.getWorkPerformedDao().renameWorkPerformed(id, name, time)
            },
            copyWithName = { item, name -> item.copy(wpDescription = name) },
        )
    }

    suspend fun syncJobSpecs(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_JOB_SPECS,
            mapCursorToItem = { cursor ->
                JobSpec(
                    jobSpecId = getLongSafe(cursor, "jobSpecId"),
                    jsName = getStringSafe(cursor, "jsName"),
                    jsIsDeleted = getBooleanSafe(cursor, "jsIsDeleted"),
                    jsUpdateTime = getStringSafe(cursor, "jsUpdateTime")
                )
            },
            getExistingById = { appDb.getJobSpecDao().getJobSpecSync(it.jobSpecId) },
            getExistingByName = { appDb.getJobSpecDao().findJobSpecByNameAnySync(it.jsName) },
            getUpdateTime = { it.jsUpdateTime },
            getName = { it.jsName },
            getId = { it.jobSpecId },
            insert = { appDb.getJobSpecDao().insertJobSpec(it) },
            update = { appDb.getJobSpecDao().updateJobSpec(it) },
            rename = { id, name, time -> appDb.getJobSpecDao().renameJobSpec(id, name, time) },
            copyWithName = { item, name -> item.copy(jsName = name) },
        )
    }

    suspend fun syncWorkOrderHistoryWorkPerformed(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_WORK_ORDER_HISTORY_WORK_PERFORMED,
            mapCursorToItem = { cursor ->
                WorkOrderHistoryWorkPerformed(
                    workOrderHistoryWorkPerformedId = getLongSafe(
                        cursor,
                        "workOrderHistoryWorkPerformedId"
                    ),
                    wowpHistoryId = getLongSafe(cursor, "wowpHistoryId"),
                    wowpWorkPerformedId = getLongSafe(cursor, "wowpWorkPerformedId"),
                    wowpAreaId = if (cursor.isNull(cursor.getColumnIndexOrThrow("wowpAreaId"))) null else cursor.getLong(
                        cursor.getColumnIndexOrThrow("wowpAreaId")
                    ),
                    wowpNote = getStringSafe(cursor, "wowpNote"),
                    wowpSequence = getIntSafe(cursor, "wowpSequence"),
                    wowpIsDeleted = getBooleanSafe(cursor, "wowpIsDeleted"),
                    wowpUpdateTime = getStringSafe(cursor, "wowpUpdateTime")
                )
            },
            getExistingById = {
                appDb.getWorkPerformedDao()
                    .getWorkOrderHistoryWorkPerformedSync(it.workOrderHistoryWorkPerformedId)
            },
            getUpdateTime = { it.wowpUpdateTime },
            insert = { appDb.getWorkPerformedDao().insertWorkOrderHistoryWorkPerformed(it) },
            update = { appDb.getWorkPerformedDao().updateWorkOrderHistoryWorkPerformed(it) },
        )
    }

    suspend fun syncWorkOrderHistoryJobSpecs(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_WORK_ORDER_HISTORY_JOB_SPECS,
            mapCursorToItem = { cursor ->
                WorkOrderJobSpec(
                    workOrderJobSpecId = getLongSafe(cursor, "workOrderJobSpecId"),
                    wojsWorkOrderId = getLongSafe(cursor, "wojsWorkOrderId"),
                    wojsJobSpecId = getLongSafe(cursor, "wojsJobSpecId"),
                    wojsAreaId = if (cursor.isNull(cursor.getColumnIndexOrThrow("wojsAreaId"))) null else cursor.getLong(
                        cursor.getColumnIndexOrThrow("wojsAreaId")
                    ),
                    wojsNote = getStringSafe(cursor, "wojsNote"),
                    wojsSequence = getIntSafe(cursor, "wojsSequence"),
                    wojsIsDeleted = getBooleanSafe(cursor, "wojsIsDeleted"),
                    wojsUpdateTime = getStringSafe(cursor, "wojsUpdateTime")
                )
            },
            getExistingById = {
                appDb.getJobSpecDao().getWorkOrderJobSpecByIdSync(it.workOrderJobSpecId)
            },
            getUpdateTime = { it.wojsUpdateTime },
            insert = { appDb.getJobSpecDao().insertWorkOrderJobSpec(it) },
            update = { appDb.getJobSpecDao().updateWorkOrderJobSpec(it) },
        )
    }

    suspend fun syncMaterials(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_MATERIALS,
            mapCursorToItem = { cursor ->
                Material(
                    materialId = getLongSafe(cursor, "materialId"),
                    mName = getStringSafe(cursor, "mName"),
                    mCost = getDoubleSafe(cursor, "mCost"),
                    mPrice = getDoubleSafe(cursor, "mPrice"),
                    mIsDeleted = getBooleanSafe(cursor, "mIsDeleted"),
                    mUpdateTime = getStringSafe(cursor, "mUpdateTime")
                )
            },
            getExistingById = { appDb.getMaterialDao().getMaterialSync(it.materialId) },
            getExistingByName = { appDb.getMaterialDao().getMaterialAnySync(it.mName) },
            getUpdateTime = { it.mUpdateTime },
            getName = { it.mName },
            getId = { it.materialId },
            insert = { appDb.getMaterialDao().insertMaterial(it) },
            update = { appDb.getMaterialDao().updateMaterial(it) },
            rename = { id, name, time -> appDb.getMaterialDao().renameMaterial(id, name, time) },
            copyWithName = { item, name -> item.copy(mName = name) },
        )
    }

    suspend fun syncWorkOrderHistoryMaterials(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_WORK_ORDER_HISTORY_MATERIALS,
            mapCursorToItem = { cursor ->
                WorkOrderHistoryMaterial(
                    workOrderHistoryMaterialId = getLongSafe(cursor, "workOrderHistoryMaterialId"),
                    wohmHistoryId = getLongSafe(cursor, "wohmHistoryId"),
                    wohmMaterialId = getLongSafe(cursor, "wohmMaterialId"),
                    wohmQuantity = getDoubleSafe(cursor, "wohmQuantity"),
                    wohmSequence = getIntSafe(cursor, "wohmSequence"),
                    wohmIsDeleted = getBooleanSafe(cursor, "wohmIsDeleted"),
                    wohmUpdateTime = getStringSafe(cursor, "wohmUpdateTime")
                )
            },
            getExistingById = {
                appDb.getMaterialDao()
                    .getWorkOrderHistoryMaterialSync(it.workOrderHistoryMaterialId)
            },
            getUpdateTime = { it.wohmUpdateTime },
            insert = { appDb.getMaterialDao().insertWorkOrderHistoryMaterial(it) },
            update = { appDb.getMaterialDao().updateWorkOrderHistoryMaterial(it) },
        )
    }

    suspend fun syncAreas(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_AREAS,
            mapCursorToItem = { cursor ->
                Areas(
                    areaId = getLongSafe(cursor, "areaId"),
                    areaName = getStringSafe(cursor, "areaName"),
                    areaIsDeleted = getBooleanSafe(cursor, "areaIsDeleted"),
                    areaUpdateTime = getStringSafe(cursor, "areaUpdateTime")
                )
            },
            getExistingById = { appDb.getAreaDao().getAreaSync(it.areaId) },
            getExistingByName = { appDb.getAreaDao().findAreaByNameAnySync(it.areaName) },
            getUpdateTime = { it.areaUpdateTime },
            getName = { it.areaName },
            getId = { it.areaId },
            insert = { appDb.getAreaDao().insertArea(it) },
            update = { appDb.getAreaDao().updateArea(it) },
            rename = { id, name, time -> appDb.getAreaDao().renameArea(id, name, time) },
            copyWithName = { item, name -> item.copy(areaName = name) },
        )
    }

    suspend fun syncJobSpecMerged(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_JOB_SPEC_MERGED,
            mapCursorToItem = { cursor ->
                JobSpecMerged(
                    jobSpecMergedId = getLongSafe(cursor, "jobSpecMergedId"),
                    jsmMasterId = getLongSafe(cursor, "jsmMasterId"),
                    jsmChildId = getLongSafe(cursor, "jsmChildId"),
                    jsmIsDeleted = getBooleanSafe(cursor, "jsmIsDeleted"),
                    jsmUpdateTime = getStringSafe(cursor, "jsmUpdateTime")
                )
            },
            getExistingById = { appDb.getJobSpecDao().getJobSpecMergedSync(it.jobSpecMergedId) },
            getUpdateTime = { it.jsmUpdateTime },
            insert = { appDb.getJobSpecDao().insertJobSpecMerged(it) },
            update = { /* No update */ },
        )
    }

    suspend fun syncMaterialMerged(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_MATERIAL_MERGED,
            mapCursorToItem = { cursor ->
                MaterialMerged(
                    materialMergeId = getLongSafe(cursor, "materialMergeId"),
                    mmMasterId = getLongSafe(cursor, "mmMasterId"),
                    mmChildId = getLongSafe(cursor, "mmChildId"),
                    mmIsDeleted = getBooleanSafe(cursor, "mmIsDeleted"),
                    mmUpdateTime = getStringSafe(cursor, "mmUpdateTime")
                )
            },
            getExistingById = { appDb.getMaterialDao().getMaterialMergedSync(it.materialMergeId) },
            getUpdateTime = { it.mmUpdateTime },
            insert = { appDb.getMaterialDao().insertMaterialMerged(it) },
            update = { /* No update */ },
        )
    }

    suspend fun syncWorkPerformedMerged(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_WORK_PERFORMED_MERGED,
            mapCursorToItem = { cursor ->
                WorkPerformedMerged(
                    workPerformedMergeId = getLongSafe(cursor, "workPerformedMergeId"),
                    wpmMasterId = getLongSafe(cursor, "wpmMasterId"),
                    wpmChildId = getLongSafe(cursor, "wpmChildId"),
                    wpmIsDeleted = getBooleanSafe(cursor, "wpmIsDeleted"),
                    wpmUpdateTime = getStringSafe(cursor, "wpmUpdateTime")
                )
            },
            getExistingById = {
                appDb.getWorkPerformedDao().getWorkPerformedMergedSync(it.workPerformedMergeId)
            },
            getUpdateTime = { it.wpmUpdateTime },
            insert = { appDb.getWorkPerformedDao().insertWorkPerformedMerged(it) },
            update = { /* No update */ },
        )
    }

    suspend fun syncWorkOrderHistoryTimeWorked(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_WORK_ORDER_HISTORY_TIME_WORKED,
            mapCursorToItem = { cursor ->
                WorkOrderHistoryTimeWorked(
                    woHistoryTimeWorkedId = getLongSafe(cursor, "woHistoryTimeWorkedId"),
                    wohtHistoryId = getLongSafe(cursor, "wohtHistoryId"),
                    wohtDateId = getLongSafe(cursor, "wohtDateId"),
                    wohtStartTime = getStringSafe(cursor, "wohtStartTime"),
                    wohtEndTime = getStringSafe(cursor, "wohtEndTime"),
                    wohtTimeType = getIntSafe(cursor, "wohtTimeType"),
                    wohtIsDeleted = getBooleanSafe(cursor, "wohtIsDeleted"),
                    wohtUpdateTime = getStringSafe(cursor, "wohtUpdateTime")
                )
            },
            getExistingById = {
                appDb.getWorkOrderTimeDao().getTimeWorkedSync(it.woHistoryTimeWorkedId)
            },
            getUpdateTime = { it.wohtUpdateTime },
            insert = { appDb.getWorkOrderTimeDao().insertTimeWorked(it) },
            update = { appDb.getWorkOrderTimeDao().updateTimeWorked(it) },
        )
    }

    suspend fun syncWorkOrderHistoryExpense(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_WORK_ORDER_HISTORY_EXPENSE,
            mapCursorToItem = { cursor ->
                WorkOrderHistoryExpense(
                    woHistoryExpenseId = getLongSafe(cursor, "woHistoryExpenseId"),
                    woheHistoryId = getLongSafe(cursor, "woheHistoryId"),
                    woheType = getStringSafe(cursor, "woheType"),
                    woheSupplier = getStringSafe(cursor, "woheSupplier"),
                    woheInvoiceNo = getStringSafe(cursor, "woheInvoiceNo"),
                    woheAmount = getDoubleSafe(cursor, "woheAmount"),
                    woheIsDeleted = getBooleanSafe(cursor, "woheIsDeleted"),
                    woheUpdateTime = getStringSafe(cursor, "woheUpdateTime")
                )
            },
            getExistingById = {
                appDb.getWorkOrderDao().getWorkOrderHistoryExpenseSync(it.woHistoryExpenseId)
            },
            getUpdateTime = { it.woheUpdateTime },
            insert = { appDb.getWorkOrderDao().insertWorkOrderHistoryExpense(it) },
            update = { appDb.getWorkOrderDao().updateWorkOrderHistoryExpense(it) },
        )
    }

    suspend fun syncSyncHistory(backupDb: SQLiteDatabase): Pair<Int, Int> {
        return syncTable(
            backupDb = backupDb,
            tableName = TABLE_SYNC_HISTORY,
            mapCursorToItem = { cursor ->
                SyncHistory(
                    syncId = getLongSafe(cursor, "syncId"),
                    syncTime = getStringSafe(cursor, "syncTime"),
                    syncSourceName = getStringSafe(cursor, "syncSourceName"),
                    syncDeviceId = getLongSafe(cursor, "syncDeviceId"),
                    syncStatus = getStringSafe(cursor, "syncStatus"),
                    syncRecordsProcessed = getStringSafe(cursor, "syncRecordsProcessed")
                )
            },
            getExistingById = { appDb.getSyncHistoryDao().getSyncHistory(it.syncId) },
            getUpdateTime = { it.syncTime },
            insert = {
                if (it.syncDeviceId != deviceId) appDb.getSyncHistoryDao().insertSyncHistory(it)
            },
            update = {
                if (it.syncDeviceId != deviceId) appDb.getSyncHistoryDao().updateSyncHistory(it)
            },
        )
    }
}