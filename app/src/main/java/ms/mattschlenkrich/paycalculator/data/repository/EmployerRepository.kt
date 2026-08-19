package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.EmployerPayRates
import ms.mattschlenkrich.paycalculator.data.entity.Employers


class EmployerRepository(private val db: PayDatabase) {

    suspend fun insertEmployer(employers: Employers) {
        val existing = db.getEmployerDao().findEmployerByNameAnySync(employers.employerName)
        if (existing != null) {
            val updated = employers.copy(
                employerId = existing.employerId,
                employerIsDeleted = false
            )
            db.getEmployerDao().updateEmployer(updated)
        } else {
            db.getEmployerDao().insertEmployer(employers)
        }
    }

    suspend fun updateEmployer(employers: Employers) = db.getEmployerDao().updateEmployer(employers)


    fun getEmployer(employerId: Long) = db.getEmployerDao().getEmployer(employerId)

    fun getEmployers() = db.getEmployerDao().getEmployers()

    suspend fun insertPayRate(payRate: EmployerPayRates) =
        db.getEmployerDao().insertPayRate(payRate)

    suspend fun updatePayRate(payRate: EmployerPayRates) =
        db.getEmployerDao().updatePayRate(payRate)

    fun getEmployerPayRates(employerId: Long) = db.getEmployerDao().getEmployerPayRates(employerId)

}