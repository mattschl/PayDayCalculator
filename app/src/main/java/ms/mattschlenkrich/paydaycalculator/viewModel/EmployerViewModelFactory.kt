package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.paydaycalculator.repository.EmployerRepository

class EmployerViewModelFactory(
    val app: Application,
    private val employerRepository: EmployerRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EmployerViewModel(app, employerRepository) as T
    }
}