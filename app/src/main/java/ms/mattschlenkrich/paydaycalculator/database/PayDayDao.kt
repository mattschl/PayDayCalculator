package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paydaycalculator.common.TABLE_PAY_PERIODS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_DATES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_DATE_EXTRAS
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtraAndTypeFull
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDates
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraTypes

@Dao
interface PayDayDao {
    @Query(
        "SELECT * FROM $TABLE_PAY_PERIODS " +
                "WHERE ppEmployerId = :employerId " +
                "ORDER BY ppCutoffDate DESC " +
                "LIMIT 4"
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

    @Delete
    suspend fun deleteWorkDateExtra(extraTypes: WorkExtraTypes)

//    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
//    @Transaction
//    @Query(
//        "SELECT $TABLE_WORK_DATES.*, $TABLE_WORK_DATE_EXTRAS.* " +
//                "FROM $TABLE_WORK_DATES " +
//                "LEFT JOIN $TABLE_WORK_DATE_EXTRAS ON " +
//                "workDateId = wdeWorkDateId " +
//                "WHERE wdEmployerId = :employerId " +
//                "AND wdCutoffDate = :cutOffDate " +
//                "ORDER BY $TABLE_WORK_DATES.wdDate, " +
//                "$TABLE_WORK_DATE_EXTRAS.wdeName COLLATE NOCASE"
//    )
//    fun getWorkDatesAndExtras(employerId: Long, cutOffDate: String):
//            LiveData<List<WorkDateAndExtras>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkDateExtra(workDateExtra: WorkDateExtras)

    @Update
    suspend fun updateWorkDateExtra(workDateExtra: WorkDateExtras)

    @Query(
        "SELECT * FROM $TABLE_WORK_DATE_EXTRAS " +
                "WHERE wdeWorkDateId = :workDateId " +
                "AND wdeIsDeleted = 0"
    )
    fun getWorkDateExtras(workDateId: Long): LiveData<List<WorkDateExtras>>

//    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
//    @Transaction
//    @Query(
//        "SELECT DISTINCT dates.* , " +
//                "extraDef.*, " +
//                "extras.* " +
//                "FROM $TABLE_WORK_DATES AS dates " +
//                "LEFT JOIN $TABLE_WORK_EXTRAS_DEFINITIONS as extraDef ON " +
//                "dates.wdEmployerId = (" +
//                "SELECT weEmployerId FROM $TABLE_WORK_EXTRAS_DEFINITIONS " +
//                "WHERE weEffectiveDate = " +
//                "(SELECT MAX(weEffectiveDate) FROM $TABLE_WORK_EXTRAS_DEFINITIONS ) " +
//                "AND weIsDeleted = 0) " +
//                "LEFT JOIN $TABLE_WORK_EXTRA_TYPES as extraType ON " +
//                "dates.wdEmployerId = (" +
//                "SELECT wetEmployerId FROM $TABLE_WORK_EXTRA_TYPES " +
//                "WHERE wetIsDeleted = 0 " +
//                "AND (wetAttachTo = 0 " +
//                "OR wetAttachTo = 1)) " +
//                "LEFT JOIN $TABLE_WORK_DATE_EXTRAS as extras ON " +
//                "dates.workDateId = extras.wdeWorkDateId " +
//                "WHERE dates.workDateId = :workDateId " +
//                "ORDER BY extraType.wetName"
//    )
//    fun getWorkDateAndExtraDefAndWorkDateExtras(workDateId: Long):
//            LiveData<List<WorkDateAndExtraDefAndWodDateExtras>>

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

    @Transaction
    @Query(
        "SELECT DISTINCT workDateExtras.*, types.* " +
                "FROM workDateExtras " +
                "LEFT JOIN " +
                "workDates ON wdeWorkDateId = ( " +
                "SELECT workDateId " +
                "FROM workDates " +
                "WHERE wdCutoffDate = :cutOff AND " +
                "wdEmployerID = :employerId AND " +
                "wdIsDeleted = 0 " +
                ") " +
                "LEFT JOIN taxTypes as types ON " +
                "taxTypeId = wdeExtraTypeId " +
                "WHERE wdeIsDeleted = 0 " +
                "ORDER BY workDates.wdDate "
    )
    fun getWorkDateExtrasPerPay(employerId: Long, cutOff: String)
            : LiveData<List<WorkDateExtraAndTypeFull>>
}