package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.model.employer.EmployerTaxTypes
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxEffectiveDates
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxTypes
import ms.mattschlenkrich.paydaycalculator.model.tax.WorkTaxRules
import ms.mattschlenkrich.paydaycalculator.repository.WorkTaxRepository

class WorkTaxViewModel(
    app: Application,
    private val workTaxRepository: WorkTaxRepository
) : AndroidViewModel(app) {

    fun insertTaxType(workTaxType: TaxTypes) =
        viewModelScope.launch {
            workTaxRepository.insertTaxType(workTaxType)
        }

    fun updateWorkTaxType(workTaxType: TaxTypes) =
        viewModelScope.launch {
            workTaxRepository.updateWorkTaxType(workTaxType)
        }

    fun getTaxTypes() =
        workTaxRepository.getTaxTypes()

    fun searchTaxTypes(query: String?) =
        workTaxRepository.searchTaxTypes(query)

    fun findTaxType(taxType: String) =
        workTaxRepository.findTaxType(taxType)

    fun insertTaxRule(taxRule: WorkTaxRules) =
        viewModelScope.launch {
            workTaxRepository.insertTaxRule(taxRule)
        }

    fun updateTaxRule(taxRule: WorkTaxRules) =
        viewModelScope.launch {
            workTaxRepository.updateTaxRule(taxRule)
        }

    fun getTaxRules(taxType: String, effectiveDate: String) =
        workTaxRepository.getTaxRules(taxType, effectiveDate)

    fun insertEffectiveDate(effectiveDate: TaxEffectiveDates) =
        viewModelScope.launch {
            workTaxRepository.insertEffectiveDate(effectiveDate)
        }

    fun getTaxEffectiveDates() =
        workTaxRepository.getTaxEffectiveDates()

    fun insertEmployerTaxType(employerTaxTypes: EmployerTaxTypes) =
        viewModelScope.launch {
            workTaxRepository.insertEmployerTaxType(employerTaxTypes)
        }

    fun updateEmployerTaxIncluded(
        employerId: Long, taxType: String, include: Boolean
    ) = viewModelScope.launch {
        workTaxRepository.updateEmployerTaxIncluded(employerId, taxType, include)
    }

    fun getEmployerTaxTypes(employerId: Long) =
        workTaxRepository.getEmployerTaxTypes(employerId)

    fun getTaxTypeAndDef(effectiveDate: String) =
        workTaxRepository.getTaxTypeAndDef(effectiveDate)

    fun getCurrentEffectiveDate(cutoffDate: String) =
        workTaxRepository.getCurrentEffectiveDate(cutoffDate)

    fun getTaxTypesByEmployer(employerId: Long) =
        workTaxRepository.getTaxTypesByEmployer(employerId)

    fun getTaxDefByDate(effectiveDate: String) =
        workTaxRepository.getTaxDefByDate(effectiveDate)
}