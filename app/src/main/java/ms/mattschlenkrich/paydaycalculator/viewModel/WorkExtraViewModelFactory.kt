package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.paydaycalculator.repository.WorkExtraRepository

@Suppress("UNCHECKED_CAST")
class WorkExtraViewModelFactory(
    val app: Application,
    private val extraRepository: WorkExtraRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkExtraViewModel(app, extraRepository) as T
    }
}