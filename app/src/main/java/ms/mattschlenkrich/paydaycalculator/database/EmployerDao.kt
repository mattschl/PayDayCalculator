package ms.mattschlenkrich.paydaycalculator.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import ms.mattschlenkrich.paydaycalculator.model.Employers

@Dao
interface EmployerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEmployer(employers: Employers)
}