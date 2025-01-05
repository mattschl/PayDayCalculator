package ms.mattschlenkrich.paycalculator.database.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.paycalculator.database.repository.WorkOrderRepository

@Suppress("UNCHECKED_CAST")
class WorkOrderViewModelFactory(
    val app: Application,
    private val workOrderRepository: WorkOrderRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkOrderViewModel(app, workOrderRepository) as T
    }
}