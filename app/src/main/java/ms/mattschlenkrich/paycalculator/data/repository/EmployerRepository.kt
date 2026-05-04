package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.EmployerPayRates
import ms.mattschlenkrich.paycalculator.data.entity.Employers


class EmployerRepository(private val db: PayDatabase) {

    suspend fun insertEmployer(employers: Employers) = db.getEmployerDao().insertEmployer(employers)

    suspend fun updateEmployer(employers: Employers) = db.getEmployerDao().updateEmployer(employers)

    suspend fun deleteEmployer(employerId: Long, updateTime: String) =
        db.getEmployerDao().deleteEmployer(employerId, updateTime)


    fun getEmployer(employerId: Long) = db.getEmployerDao().getEmployer(employerId)

    fun getEmployers() = db.getEmployerDao().getEmployers()

    fun searchEmployers(query: String?) = db.getEmployerDao().searchEmployers(query)

    fun findEmployer(employerName: String) = db.getEmployerDao().findEmployer(employerName)

    suspend fun insertPayRate(payRate: EmployerPayRates) =
        db.getEmployerDao().insertPayRate(payRate)

    suspend fun updatePayRate(payRate: EmployerPayRates) =
        db.getEmployerDao().updatePayRate(payRate)

    fun getEmployerPayRates(employerId: Long) = db.getEmployerDao().getEmployerPayRates(employerId)

    fun getCurrentEmployerRate(employerId: Long, cutoffDate: String) =
        db.getEmployerDao().getCurrentEmployerRate(employerId, cutoffDate)
}