package ms.mattschlenkrich.paydaycalculator.repository


import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.model.Employers


class EmployerRepository(private val db: PayDatabase) {

    suspend fun insertEmployer(employers: Employers) =
        db.getEmployerDao().insertEmployer(employers)

    suspend fun updateEmployer(employers: Employers) =
        db.getEmployerDao().updateEmployer(employers)

    fun getCurrentEmployers() =
        db.getEmployerDao().getCurrentEmployers()
}