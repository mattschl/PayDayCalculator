package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paycalculator.data.entity.EmployerPayRates
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.repository.EmployerRepository

class EmployerViewModel(
    val app: Application,
    private val employerRepository: EmployerRepository
) : AndroidViewModel(app) {

    suspend fun insertEmployer(employers: Employers) =
        employerRepository.insertEmployer(employers)

    suspend fun updateEmployer(employers: Employers) =
        employerRepository.updateEmployer(employers)

    fun getEmployer(employerId: Long) =
        employerRepository.getEmployer(employerId)

    val employersAll = employerRepository.getEmployers()

    fun getEmployers() =
        employerRepository.getEmployers()

    suspend fun insertPayRate(payRate: EmployerPayRates) =
        employerRepository.insertPayRate(payRate)

    suspend fun updatePayRate(payRate: EmployerPayRates) =
        employerRepository.updatePayRate(payRate)

    fun getEmployerPayRates(employerId: Long) =
        employerRepository.getEmployerPayRates(employerId)

}