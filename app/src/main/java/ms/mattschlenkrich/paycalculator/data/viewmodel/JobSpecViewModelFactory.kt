package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.paycalculator.data.repository.JobSpecRepository

class JobSpecViewModelFactory(
    private val app: Application,
    private val jobSpecRepository: JobSpecRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JobSpecViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JobSpecViewModel(app, jobSpecRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}