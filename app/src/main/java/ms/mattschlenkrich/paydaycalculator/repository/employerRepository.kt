package ms.mattschlenkrich.paydaycalculator.repository


import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.database.PayDatabase


class EmployerRepository(private val db: PayDatabase) {

    suspend fun insertEmployer(employers: Employers) =
        db.getEmployerDao().insertEmployer(employers)
}