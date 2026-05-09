package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.paycalculator.data.repository.WorkTimeRepository

@Suppress("UNCHECKED_CAST")
class WorkTimeViewModelFactory(
    val app: Application,
    private val workTimeRepository: WorkTimeRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkTimeViewModel(app, workTimeRepository) as T
    }
}