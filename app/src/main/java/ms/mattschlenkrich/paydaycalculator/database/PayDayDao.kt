package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ms.mattschlenkrich.paydaycalculator.common.TABLE_PAY_PERIODS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_DATES
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods
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
    suspend fun insertCutOffDate(cutOff: PayPeriods)

    @Query(
        "SELECT * FROM $TABLE_WORK_DATES " +
                "WHERE wdEmployerId = :employerId " +
                "AND wdCutoffDate = :cutOff " +
                "ORDER BY wdDate"
    )
    fun getWorkDateList(employerId: Long, cutOff: String): LiveData<List<WorkDates>>
}