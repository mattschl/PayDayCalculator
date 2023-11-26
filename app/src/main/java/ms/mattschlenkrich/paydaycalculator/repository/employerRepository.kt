package ms.mattschlenkrich.paydaycalculator.repository


import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.model.Employers


class EmployerRepository(private val db: PayDatabase) {

    suspend fun insertEmployer(employers: Employers) =
        db.getEmployerDao().insertEmployer(employers)

    fun getCurrentEmployers() =
        db.getEmployerDao().getCurrentEmployers()
}