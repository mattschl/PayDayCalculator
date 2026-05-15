package ms.mattschlenkrich.paycalculator.data.viewmodel

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

    fun getEmployerTaxTypes(employerId: Long) =
        workTaxRepository.getEmployerTaxTypes(employerId)
}