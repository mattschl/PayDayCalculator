package ms.mattschlenkrich.paycalculator.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.data.entity.EmployerTaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.TaxEffectiveDates
import ms.mattschlenkrich.paycalculator.data.entity.TaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkTaxRules
import ms.mattschlenkrich.paycalculator.data.repository.WorkTaxRepository

class WorkTaxViewModel(
    app: Application,
    private val workTaxRepository: WorkTaxRepository
) : AndroidViewModel(app) {

    fun insertTaxType(workTaxType: TaxTypes) =
        viewModelScope.launch {
            workTaxRepository.insertTaxType(workTaxType)
        }

    fun insertTaxTypeWithEmployerLinks(
        taxType: TaxTypes,
        employers: List<Employers>,
        currentTime: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        workTaxRepository.insertTaxType(taxType)
        employers.forEach { employer ->
            workTaxRepository.insertEmployerTaxType(
                EmployerTaxTypes(
                    etrEmployerId = employer.employerId,
                    etrTaxType = taxType.taxType,
                    etrInclude = false,
                    etrIsDeleted = false,
                    etrUpdateTime = currentTime
                )
            )
        }
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

    fun updateEmployerTaxType(employerTaxTypes: EmployerTaxTypes) =
        viewModelScope.launch(Dispatchers.IO) {
            workTaxRepository.updateEmployerTaxType(employerTaxTypes)
        }

    fun updateEmployerTaxIncluded(
        employerId: Long, taxType: String, include: Boolean
    ) = viewModelScope.launch {
        workTaxRepository.updateEmployerTaxIncluded(employerId, taxType, include)
    }

    fun getEmployerTaxTypes(employerId: Long) =
        workTaxRepository.getEmployerTaxTypes(employerId)

//    fun getTaxTypeAndDef(effectiveDate: String) =
//        workTaxRepository.getTaxTypeAndDef(effectiveDate)

    fun getCurrentEffectiveDate(cutoffDate: String) =
        workTaxRepository.getCurrentEffectiveDate(cutoffDate)

    fun getTaxTypesByEmployer(employerId: Long) =
        workTaxRepository.getTaxTypesByEmployer(employerId)

    fun getTaxDefByDate(effectiveDate: String) =
        workTaxRepository.getTaxDefByDate(effectiveDate)
}