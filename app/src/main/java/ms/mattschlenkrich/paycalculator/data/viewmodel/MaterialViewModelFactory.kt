package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.paycalculator.data.repository.MaterialRepository

class MaterialViewModelFactory(
    private val app: Application,
    private val materialRepository: MaterialRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MaterialViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MaterialViewModel(app, materialRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}