package ms.mattschlenkrich.paycalculator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.common.EMPLOYER_NAME
import ms.mattschlenkrich.paycalculator.common.TABLE_EMPLOYERS
import ms.mattschlenkrich.paycalculator.common.TABLE_EMPLOYER_PAY_RATES
import ms.mattschlenkrich.paycalculator.data.entity.EmployerPayRates
import ms.mattschlenkrich.paycalculator.data.entity.Employers

@Dao
interface EmployerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEmployer(employers: Employers)

    @Update
    suspend fun updateEmployer(employers: Employers)

    @Query(
        "SELECT * FROM $TABLE_EMPLOYERS " +
                "WHERE employerId = :employerId " +
                "AND employerIsDeleted = 0"
    )
    fun getEmployer(employerId: Long): LiveData<Employers>

    @Query(
        "SELECT * FROM $TABLE_EMPLOYERS " +
                "WHERE employerIsDeleted = 0 " +
                "ORDER BY $EMPLOYER_NAME COLLATE NOCASE"
    )
    fun getEmployers(): LiveData<List<Employers>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPayRate(payRate: EmployerPayRates)

    @Update
    suspend fun updatePayRate(payRate: EmployerPayRates)

    @Query(
        "SELECT * FROM $TABLE_EMPLOYER_PAY_RATES " +
                "WHERE eprEmployerId = :employerId " +
                "AND eprIsDeleted = 0 " +
                "ORDER BY eprEffectiveDate DESC"
    )
    fun getEmployerPayRates(employerId: Long): LiveData<List<EmployerPayRates>>
}