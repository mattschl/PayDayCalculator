package ms.mattschlenkrich.paycalculator.database.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.paycalculator.database.repository.PayDetailRepository

@Suppress("UNCHECKED_CAST")
class PayDetailViewModelFactory(
    val app: Application,
    private val payDetailRepository: PayDetailRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PayDetailViewModel(app, payDetailRepository) as T
    }
}