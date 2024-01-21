package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ms.mattschlenkrich.paydaycalculator.common.TABLE_PAY_PERIODS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_DATES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_DATES_EXTRAS
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods
import ms.mattschlenkrich.paydaycalculator.model.WorkDateAndExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDates

@Dao
interface PayDayDao {
    @Query(
        "SELECT * FROM $TABLE_PAY_PERIODS " +
                "WHERE ppEmployerId = :employerId " +
                "ORDER BY ppCutoffDate DESC"
    )
    fun getCutOffDates(employerId: Long): LiveData<List<PayPeriods>>

    @Insert
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

    @Query(
        "SELECT $TABLE_WORK_DATES.*, $TABLE_WORK_DATES_EXTRAS.* " +
                "FROM $TABLE_WORK_DATES " +
                "LEFT JOIN $TABLE_WORK_DATES_EXTRAS ON " +
                "workDateId = wdId " +
                "WHERE wdEmployerId = :employerId " +
                "AND wdCutoffDate = :cutOffDate " +
                "ORDER BY $TABLE_WORK_DATES.wdDate, " +
                "$TABLE_WORK_DATES_EXTRAS.wdeName COLLATE NOCASE"
    )
    fun getWorkDatesAndExtras(employerId: Long, cutOffDate: String):
            LiveData<List<WorkDateAndExtras>>
}