package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.model.TaxEffectiveDates
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxRules
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

    fun getTaxTypes() =
        workTaxRepository.getTaxTypes()

    fun insertTaxRule(taxRule: WorkTaxRules) =
        viewModelScope.launch {
            workTaxRepository.insertTaxRule(taxRule)
        }

    fun updateTaxRule(taxRule: WorkTaxRules) =
        viewModelScope.launch {
            workTaxRepository.updateTaxRule(taxRule)
        }

    fun getTaxRules() =
        workTaxRepository.getTaxRules()

    fun insertEffectiveDate(effectiveDate: TaxEffectiveDates) =
        viewModelScope.launch {
            workTaxRepository.insertEffectiveDate(effectiveDate)
        }

    fun getTaxEffectiveDates() =
        workTaxRepository.getTaxEffectiveDates()
}