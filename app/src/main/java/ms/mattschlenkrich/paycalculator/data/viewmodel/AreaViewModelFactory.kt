package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.paycalculator.data.repository.AreaRepository

class AreaViewModelFactory(
    private val app: Application,
    private val areaRepository: AreaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AreaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AreaViewModel(app, areaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}