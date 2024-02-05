package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomWarnings
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paydaycalculator.common.TABLE_PAY_PERIODS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_DATES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_DATE_EXTRAS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRAS_DEFINITIONS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRA_TYPES
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods
import ms.mattschlenkrich.paydaycalculator.model.WorkDateAndExtraDefAndWodDateExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDateAndExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDates

@Dao
interface PayDayDao {
    @Query(
        "SELECT * FROM $TABLE_PAY_PERIODS " +
                "WHERE ppEmployerId = :employerId " +
                "ORDER BY ppCutoffDate DESC"
    )
    fun getCutOffDates(employerId: Long): LiveData<List<PayPeriods>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPayPeriod(payPeriod: PayPeriods)

    @Query(
        "SELECT * FROM $TABLE_WORK_DATES " +
                "WHERE wdEmployerId = :employerId " +
                "AND wdCutoffDate = :cutOff " +
                "ORDER BY wdDate"
    )
    fun getWorkDateList(employerId: Long, cutOff: String): LiveData<List<WorkDates>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkDate(workDate: WorkDates)

    @Update
    suspend fun updateWorkDate(workDate: WorkDates)

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query(
        "SELECT $TABLE_WORK_DATES.*, $TABLE_WORK_DATE_EXTRAS.* " +
                "FROM $TABLE_WORK_DATES " +
                "LEFT JOIN $TABLE_WORK_DATE_EXTRAS ON " +
                "workDateId = wdeWorkDateId " +
                "WHERE wdEmployerId = :employerId " +
                "AND wdCutoffDate = :cutOffDate " +
                "ORDER BY $TABLE_WORK_DATES.wdDate, " +
                "$TABLE_WORK_DATE_EXTRAS.wdeName COLLATE NOCASE"
    )
    fun getWorkDatesAndExtras(employerId: Long, cutOffDate: String):
            LiveData<List<WorkDateAndExtras>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkDateExtra(workDateExtra: WorkDateExtras)

    @Update
    suspend fun updateWorkDateExtra(workDateExtra: WorkDateExtras)

    @Query(
        "SELECT * FROM $TABLE_WORK_DATE_EXTRAS " +
                "WHERE wdeWorkDateId = :workDateId"
    )
    fun getWorkDateExtras(workDateId: Long): LiveData<List<WorkDateExtras>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query(
        "SELECT dates.* , " +
                "extraDef.*, " +
                "extras.* " +
                "FROM $TABLE_WORK_DATES AS dates " +
                "LEFT JOIN $TABLE_WORK_EXTRAS_DEFINITIONS as extraDef ON " +
                "dates.wdEmployerId = (" +
                "SELECT weEmployerId FROM $TABLE_WORK_EXTRAS_DEFINITIONS " +
                "WHERE weEffectiveDate = " +
                "(SELECT MAX(weEffectiveDate) FROM $TABLE_WORK_EXTRAS_DEFINITIONS) " +
                "AND weIsDeleted = 0) " +
                "LEFT JOIN $TABLE_WORK_EXTRA_TYPES as extraType ON " +
                "dates.wdEmployerId = (" +
                "SELECT wetEmployerId FROM $TABLE_WORK_EXTRA_TYPES " +
                "WHERE wetIsDeleted = 0) " +
                "LEFT JOIN $TABLE_WORK_DATE_EXTRAS as extras ON " +
                "dates.workDateId = extras.wdeWorkDateId " +
                "WHERE dates.workDateId = :workDateId " +
                "ORDER BY extraType.wetName"
    )
    fun getWorkDateAndExtraDefAndWorkDateExtras(workDateId: Long):
            LiveData<List<WorkDateAndExtraDefAndWodDateExtras>>
}