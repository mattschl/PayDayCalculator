package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.model.Employers
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

    fun getEmployers() =
        employerRepository.getEmployers()

    fun searchEmployers(query: String?) =
        employerRepository.searchEmployers(query)
}