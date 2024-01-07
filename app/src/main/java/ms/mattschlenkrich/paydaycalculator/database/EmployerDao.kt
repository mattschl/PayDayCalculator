package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_NAME
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYERS
import ms.mattschlenkrich.paydaycalculator.model.Employers

@Dao
interface EmployerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEmployer(employers: Employers)

    @Update
    suspend fun updateEmployer(employers: Employers)

    @Query(
        "SELECT * FROM $TABLE_EMPLOYERS " +
                "WHERE employerIsDeleted = 0 " +
                "ORDER BY $EMPLOYER_NAME COLLATE NOCASE"
    )
    fun getEmployers(): LiveData<List<Employers>>

    @Query(
        "SELECT * FROM $TABLE_EMPLOYERS " +
                "WHERE employerName LIKE :query " +
                "ORDeR BY employerName COLLATE NOCASE"
    )
    fun searchEmployers(query: String?): LiveData<List<Employers>>

    @Query(
        "SELECT * FROM $TABLE_EMPLOYERS " +
                "WHERE employerName = :employerName"
    )
    fun findEmployer(employerName: String): LiveData<Employers>

}