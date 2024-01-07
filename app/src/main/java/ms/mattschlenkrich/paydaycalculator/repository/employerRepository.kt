package ms.mattschlenkrich.paydaycalculator.repository


import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.model.Employers


class EmployerRepository(private val db: PayDatabase) {

    suspend fun insertEmployer(employers: Employers) =
        db.getEmployerDao().insertEmployer(employers)

    suspend fun updateEmployer(employers: Employers) =
        db.getEmployerDao().updateEmployer(employers)

    fun getEmployers() =
        db.getEmployerDao().getEmployers()

    fun searchEmployers(query: String?) =
        db.getEmployerDao().searchEmployers(query)

    fun findEmployer(employerName: String) =
        db.getEmployerDao().findEmployer(employerName)
}