package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.paycalculator.data.repository.WorkPerformedRepository

class WorkPerformedViewModelFactory(
    private val app: Application,
    private val workPerformedRepository: WorkPerformedRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkPerformedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkPerformedViewModel(app, workPerformedRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}