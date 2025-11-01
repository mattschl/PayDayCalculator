package ms.mattschlenkrich.paycalculator.database.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerPayRates
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.repository.EmployerRepository
import ms.mattschlenkrich.paycalculator.logic.employer.EmployerLogicViewModel

class EmployerViewModel(
    val app: Application,
    private val employerRepository: EmployerRepository
) : AndroidViewModel(app) {
    val employerLogicViewModel: EmployerLogicViewModel =
        EmployerLogicViewModel(app, this)

    fun getCurrentEmployer(): Employers {
        return employerLogicViewModel.currentEmployerObj.getEmployer()
    }

    fun getEmployerList(): List<Employers> {
        return employerLogicViewModel.getEmployerList()
    }

    fun insertEmployer(employers: Employers) =
        viewModelScope.launch {
            employerRepository.insertEmployer(employers)
        }

    fun updateEmployer(employers: Employers) =
        viewModelScope.launch {
            employerRepository.updateEmployer(employers)
        }

    fun deleteEmployer(employerId: Long, updateTime: String) = viewModelScope.launch {
        employerRepository.deleteEmployer(employerId, updateTime)
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

    fun getCurrentEmployerRate(employerId: Long, cutoffDate: String) =
        employerRepository.getCurrentEmployerRate(employerId, cutoffDate)
}