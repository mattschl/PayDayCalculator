package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.model.employer.EmployerPayRates
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.repository.EmployerRepository

class EmployerViewModel(
    app: Application,
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

    fun getEmployers() =
        employerRepository.getEmployers()

    fun searchEmployers(query: String?) =
        employerRepository.searchEmployers(query)

    fun findEmployer(employerName: String) =
        employerRepository.findEmployer(employerName)

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