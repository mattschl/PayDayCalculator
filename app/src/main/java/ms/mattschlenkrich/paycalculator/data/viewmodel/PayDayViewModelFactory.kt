package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.paycalculator.data.repository.PayDayRepository

@Suppress("UNCHECKED_CAST")
class PayDayViewModelFactory(
    val app: Application,
    private val payDayRepository: PayDayRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PayDayViewModel(app, payDayRepository) as T
    }
}