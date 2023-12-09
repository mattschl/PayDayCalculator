package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.paydaycalculator.repository.WorkTaxRepository

@Suppress("UNCHECKED_CAST")
class WorkTaxViewModelFactory(
    val app: Application,
    private val workTaxRepository: WorkTaxRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkTaxViewModel(app, workTaxRepository) as T
    }
}