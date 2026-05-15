package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.data.entity.EmployerPayRates
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.repository.EmployerRepository

class EmployerViewModel(
    val app: Application,
    private val employerRepository: EmployerRepository
) : AndroidViewModel(app) {

    fun insertEmployer(employers: Employers) =
        viewModelScope.launch {
            employerRepository.insertEmployer(employers)
        }

    fun updateEmployer(employers: Employers) =
        viewModelScope.launch {
            employerRepository.updateEmployer(employers)
        }

    fun getEmployer(employerId: Long) =
        employerRepository.getEmployer(employerId)

    val employersAll = employerRepository.getEmployers()

    fun getEmployers() =
        employerRepository.getEmployers()

    fun insertPayRate(payRate: EmployerPayRates) =
        viewModelScope.launch {
            employerRepository.insertPayRate(payRate)
        }

    fun updatePayRate(payRate: EmployerPayRates) =
        viewModelScope.launch {
            employerRepository.updatePayRate(payRate)
        }

    fun getEmployerPayRates(employerId: Long) =
        employerRepository.getEmployerPayRates(employerId)

}