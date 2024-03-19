package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomWarnings
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paydaycalculator.common.TABLE_PAY_PERIODS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_DATES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_DATE_EXTRAS
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtraAndTypeFull
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtrasAndDates
import ms.mattschlenkrich.paydaycalculator.model.WorkDates
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.model.WorkPayPeriodExtras

@Dao
interface PayDayDao {
    @Query(
        "SELECT * FROM $TABLE_PAY_PERIODS " +
                "WHERE ppEmployerId = :employerId " +
                "AND ppIsDeleted = 0 " +
                "ORDER BY ppCutoffDate DESC " +
                "LIMIT 8"
    )
    fun getCutOffDates(employerId: Long): LiveData<List<PayPeriods>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPayPeriod(payPeriod: PayPeriods)

    @Update
    suspend fun updatePayPeriod(payPeriod: PayPeriods)

    @Query(
        "SELECT * FROM payPeriods " +
                "WHERE ppCutoffDate = :cutOff " +
                "AND ppEmployerId = :employerId " +
                "AND ppIsDeleted = 0"
    )
    fun getPayPeriod(cutOff: String, employerId: Long): LiveData<PayPeriods>

    @Query(
        "SELECT * FROM $TABLE_WORK_DATES " +
                "WHERE wdEmployerId = :employerId " +
                "AND wdCutoffDate = :cutOff " +
                "AND wdIsDeleted = 0 " +
                "ORDER BY wdDate"
    )
    fun getWorkDateList(employerId: Long, cutOff: String): LiveData<List<WorkDates>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkDate(workDate: WorkDates)

    @Update
    suspend fun updateWorkDate(workDate: WorkDates)

    @Delete
    suspend fun deleteWorkDateExtra(extraTypes: WorkExtraTypes)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkDateExtra(workDateExtra: WorkDateExtras)

    @Update
    suspend fun updateWorkDateExtra(workDateExtra: WorkDateExtras)

    @Query(
        "SELECT * FROM $TABLE_WORK_DATE_EXTRAS " +
                "WHERE wdeWorkDateId = :workDateId "
    )
    fun getWorkDateExtras(workDateId: Long): LiveData<List<WorkDateExtras>>

    @Query(
        "SELECT * FROM $TABLE_WORK_DATE_EXTRAS " +
                "WHERE wdeWorkDateId = :workDateId " +
                "AND wdeIsDeleted = 0"
    )

    fun getWorkDateExtrasActive(workDateId: Long): LiveData<List<WorkDateExtras>>

    @Query(
        "Update $TABLE_WORK_DATE_EXTRAS " +
                "SET wdeIsDeleted = 1, " +
                "wdeUpdateTime = :updateTime " +
                "WHERE wdeWorkDateId = :workDateId " +
                "AND wdeName = :extraName"
    )
    suspend fun deleteWorkDateExtra(
        extraName: String, workDateId: Long, updateTime: String
    )

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query(
        "SELECT DISTINCT workDateExtras.*, types.*, defs.* " +
                "FROM workDateExtras " +
                "LEFT JOIN " +
                "workDates ON wdeWorkDateId = ( " +
                "SELECT workDateId " +
                "FROM workDates " +
                "WHERE wdCutoffDate = :cutOff AND " +
                "wdEmployerID = :employerId AND " +
                "wdIsDeleted = 0 " +
                ") " +
                "LEFT JOIN workExtraTypes as types ON " +
                "workExtraTypeId = wdeExtraTypeId " +
                "LEFT JOIN workTaxRules as defs ON " +
                "wdeExtraTypeId = wtType " +
                " WHERE wdeIsDeleted = 0 " +
                "ORDER BY wdeName "
    )
    fun getWorkDateExtrasPerPay(employerId: Long, cutOff: String)
            : LiveData<List<WorkDateExtraAndTypeFull>>

    @Insert
    suspend fun insertPayPeriodExtra(payPeriodExtra: WorkPayPeriodExtras)

    @Update
    suspend fun updatePayPeriodExtra(payPeriodExtra: WorkPayPeriodExtras)

    @Query(
        "SELECT * FROM workPayPeriodExtras " +
                "WHERE workPayPeriodExtraId = :workPayPeriodExtraId"
    )
    fun findPayPeriodExtra(workPayPeriodExtraId: Long): LiveData<WorkPayPeriodExtras>

    @Query(
        "SELECT * FROM workPayPeriodExtras " +
                "WHERE workPayPeriodExtraId = :extraName"
    )
    fun findPayPeriodExtra(extraName: String): LiveData<WorkPayPeriodExtras>

    @Query(
        "SELECT * FROM workPayPeriodExtras " +
                "WHERE ppePayPeriodId = :payPeriodId " +
                "AND ppeAttachTo = 3 " +
                "ORDER BY ppeName"
    )
    fun getPayPeriodExtras(payPeriodId: Long): LiveData<List<WorkPayPeriodExtras>>

    @Query(
        "SELECT * FROM workDates " +
                "JOIN (" +
                "SELECT * FROM workDateExtras " +
                "WHERE wdeIsDeleted = 0 " +
                ") ON workDateId = wdeWorkDateId " +
                "WHERE wdIsDeleted =0 " +
                "AND wdCutoffDate = :cutOffDate " +
                "ORDER BY wdeName , wdDate "
    )
    fun getWorkDateExtrasAndDates(cutOffDate: String):
            LiveData<List<WorkDateExtrasAndDates>>
}