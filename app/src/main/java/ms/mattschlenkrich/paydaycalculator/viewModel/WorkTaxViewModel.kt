package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxTypes
import ms.mattschlenkrich.paydaycalculator.repository.WorkTaxRepository

class WorkTaxViewModel(
    app: Application,
    private val workTaxRepository: WorkTaxRepository
) : AndroidViewModel(app) {

    fun insertTaxType(workTaxType: WorkTaxTypes) =
        viewModelScope.launch {
            workTaxRepository.insertTaxType(workTaxType)
        }

    fun updateWorkTaxType(workTaxType: WorkTaxTypes) =
        viewModelScope.launch {
            workTaxRepository.updateWorkTaxType(workTaxType)
        }

    fun getWorkTypes() =
        workTaxRepository.getWorkTypes()
}