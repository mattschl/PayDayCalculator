package ms.mattschlenkrich.paydaycalculator.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_NAME
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYERS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYER_PAY_RATES
import ms.mattschlenkrich.paydaycalculator.database.model.employer.EmployerPayRates
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers

@Dao
interface EmployerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEmployer(employers: Employers)

    @Update
    suspend fun updateEmployer(employers: Employers)

    @Query(
        "SELECT * FROM $TABLE_EMPLOYERS " +
                "WHERE employerId = :employerId"
    )
    fun getEmployer(employerId: Long): LiveData<Employers>

    @Query(
        "SELECT * FROM $TABLE_EMPLOYERS " +
                "WHERE employerIsDeleted = 0 " +
                "ORDER BY $EMPLOYER_NAME COLLATE NOCASE"
    )
    fun getEmployers(): LiveData<List<Employers>>

    @Query(
        "SELECT * FROM $TABLE_EMPLOYERS " +
                "WHERE employerName LIKE :query " +
                "ORDER BY employerName COLLATE NOCASE"
    )
    fun searchEmployers(query: String?): LiveData<List<Employers>>

    @Query(
        "SELECT * FROM $TABLE_EMPLOYERS " +
                "WHERE employerName = :employerName"
    )
    fun findEmployer(employerName: String): Employers

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayRate(payRate: EmployerPayRates)

    @Update
    suspend fun updatePayRate(payRate: EmployerPayRates)

    @Query(
        "SELECT * FROM $TABLE_EMPLOYER_PAY_RATES " +
                "WHERE eprEmployerId = :employerId " +
                "ORDER BY eprEffectiveDate DESC"
    )
    fun getEmployerPayRates(employerId: Long): LiveData<List<EmployerPayRates>>

    @Query(
        "SELECT * FROM employerPayRates " +
                "WHERE eprEmployerId = :employerId " +
                "AND eprEffectiveDate <= :cutoffDate " +
                "AND eprIsDeleted = 0 " +
                "ORDER BY eprEffectiveDate DESC " +
                "LIMIT 1"
    )
    fun getCurrentEmployerRate(employerId: Long, cutoffDate: String):
            LiveData<EmployerPayRates>
}