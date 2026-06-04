package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

    suspend fun insertTaxTypeWithEmployerLinks(
        taxType: TaxTypes,
        employers: List<Employers>,
        currentTime: String
    ) {
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

    suspend fun updateWorkTaxType(workTaxType: TaxTypes) =
        workTaxRepository.updateWorkTaxType(workTaxType)

    fun getTaxTypes() =
        workTaxRepository.getTaxTypes()

    suspend fun getTaxTypesSync() =
        workTaxRepository.getTaxTypesSync()

    suspend fun insertTaxRule(taxRule: WorkTaxRules) =
        workTaxRepository.insertTaxRule(taxRule)

    suspend fun updateTaxRule(taxRule: WorkTaxRules) =
        workTaxRepository.updateTaxRule(taxRule)

    fun getTaxRules(taxType: String, effectiveDate: String) =
        workTaxRepository.getTaxRules(taxType, effectiveDate)

    suspend fun insertEffectiveDate(effectiveDate: TaxEffectiveDates) =
        workTaxRepository.insertEffectiveDate(effectiveDate)

    fun getTaxEffectiveDates() =
        workTaxRepository.getTaxEffectiveDates()

    suspend fun insertEmployerTaxType(employerTaxTypes: EmployerTaxTypes) =
        workTaxRepository.insertEmployerTaxType(employerTaxTypes)

    suspend fun updateEmployerTaxType(employerTaxTypes: EmployerTaxTypes) =
        workTaxRepository.updateEmployerTaxType(employerTaxTypes)

    fun getEmployerTaxTypes(employerId: Long) =
        workTaxRepository.getEmployerTaxTypes(employerId)
}