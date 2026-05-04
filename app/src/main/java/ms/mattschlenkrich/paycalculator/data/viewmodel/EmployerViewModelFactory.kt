package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.paycalculator.data.repository.EmployerRepository

@Suppress("UNCHECKED_CAST")
class EmployerViewModelFactory(
    val app: Application,
    private val employerRepository: EmployerRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EmployerViewModel(app, employerRepository) as T
    }
}