package ms.mattschlenkrich.paydaycalculator.repository


import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.model.employer.EmployerPayRates
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers


class EmployerRepository(private val db: PayDatabase) {

    suspend fun insertEmployer(employers: Employers) =
        db.getEmployerDao().insertEmployer(employers)

    suspend fun updateEmployer(employers: Employers) =
        db.getEmployerDao().updateEmployer(employers)

    fun getEmployer(employerId: Long) =
        db.getEmployerDao().getEmployer(employerId)

    fun getEmployers() =
        db.getEmployerDao().getEmployers()

    fun searchEmployers(query: String?) =
        db.getEmployerDao().searchEmployers(query)

    fun findEmployer(employerName: String) =
        db.getEmployerDao().findEmployer(employerName)

    suspend fun insertPayRate(payRate: EmployerPayRates) =
        db.getEmployerDao().insertPayRate(payRate)

    suspend fun updatePayRate(payRate: EmployerPayRates) =
        db.getEmployerDao().updatePayRate(payRate)

    fun getEmployerPayRates(employerId: Long) =
        db.getEmployerDao().getEmployerPayRates(employerId)
}