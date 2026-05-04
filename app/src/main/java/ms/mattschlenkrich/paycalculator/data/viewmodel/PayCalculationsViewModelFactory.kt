package ms.mattschlenkrich.paycalculator.data

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.paycalculator.data.repository.PayCalculationsRepository

@Suppress("UNCHECKED_CAST")
class PayCalculationsViewModelFactory(
    val app: Application,
    private val payCalculationsRepository: PayCalculationsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PayCalculationsViewModel(app, payCalculationsRepository) as T
    }
}