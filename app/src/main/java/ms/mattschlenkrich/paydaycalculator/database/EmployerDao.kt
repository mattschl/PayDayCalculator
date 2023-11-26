package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_IS_DELETED
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_NAME
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYERS
import ms.mattschlenkrich.paydaycalculator.model.Employers

@Dao
interface EmployerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEmployer(employers: Employers)

    @Query(
        "SELECT * FROM $TABLE_EMPLOYERS " +
                "WHERE $EMPLOYER_IS_DELETED = 0 " +
                "ORDER BY $EMPLOYER_NAME COLLATE NOCASE"
    )
    fun getCurrentEmployers(): LiveData<List<Employers>>

}