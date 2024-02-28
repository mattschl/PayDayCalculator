package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paydaycalculator.model.EmployerPayRates
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.ExtraDefinitionFull
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods
import ms.mattschlenkrich.paydaycalculator.model.TaxEffectiveDates
import ms.mattschlenkrich.paydaycalculator.model.TaxTypes
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDates
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxRules

class MainViewModel(
    app: Application,
) : AndroidViewModel(app) {
    private var employer: Employers? = null
    private var employerString: String? = null
    private var taxType: TaxTypes? = null
    private var taxTypeString: String? = null
    private var taxRule: WorkTaxRules? = null
    private var effectiveDate: TaxEffectiveDates? = null
    private var effectiveDateString: String? = null
    private var taxLevel: Int? = null
    private var callingFragment: String? = null
    private var extraDefinitionFull: ExtraDefinitionFull? = null
    private var extraType: WorkExtraTypes? = null
    private var workDateExtraList = ArrayList<WorkDateExtras>()
    private var workDate: String? = null
    private var workDateObject: WorkDates? = null
    private var cutOffDate: String? = null
    private var payPeriod: PayPeriods? = null
    private var payRate: EmployerPayRates? = null

    fun setWorkDateExtraList(extraList: ArrayList<WorkDateExtras>) {
        workDateExtraList = extraList
    }

    fun getWorkDateExtraList(): ArrayList<WorkDateExtras> {
        return workDateExtraList
    }

    fun eraseWorkDateExtraList() {
        workDateExtraList.clear()
    }

    fun setPayRate(newRate: EmployerPayRates?) {
        payRate = newRate
    }

    fun getPayRate(): EmployerPayRates? {
        return payRate
    }

    fun getWorkExtraType(): WorkExtraTypes? {
        return extraType
    }

    fun setWorkExtraType(newExtra: WorkExtraTypes?) {
        extraType = newExtra
    }

    fun setWorkDateObject(newDate: WorkDates?) {
        workDateObject = newDate
    }

    fun getWorkDateObject(): WorkDates? {
        return workDateObject
    }

    fun setPayPeriod(newPayPeriod: PayPeriods?) {
        payPeriod = newPayPeriod
    }

    fun getPayPeriod(): PayPeriods? {
        return payPeriod
    }

    fun setCutOffDate(date: String?) {
        cutOffDate = date
    }

    fun getCutOffDate(): String? {
        return cutOffDate
    }

    fun setWorkDateString(date: String?) {
        workDate = date
    }

    fun getWorkDateString(): String? {
        return workDate
    }

    fun setEmployerString(employer: String?) {
        employerString = employer
    }

    fun getEmployerString(): String? {
        return employerString
    }

    fun setExtraDefinitionFull(newExtra: ExtraDefinitionFull?) {
        extraDefinitionFull = newExtra
    }

    fun getExtraDefinitionFull(): ExtraDefinitionFull? {
        return extraDefinitionFull
    }

    fun setCallingFragment(newFragment: String?) {
        callingFragment = newFragment
    }

    fun addCallingFragment(newFragment: String?) {
        if (callingFragment != null) {
            callingFragment += ", $newFragment"
        } else {
            callingFragment = newFragment
        }
    }

    fun removeCallingFragment(oldFragment: String) {
        callingFragment = if (callingFragment != null) {
            callingFragment!!.replace(", $oldFragment", "")
        } else {
            null
        }
    }

    fun getCallingFragment(): String? {
        return callingFragment
    }

    fun setEffectiveDateString(newDate: String?) {
        effectiveDateString = newDate
    }

    fun getEffectiveDateString(): String? {
        return effectiveDateString
    }

    fun setTaxTypeString(newType: String?) {
        taxTypeString = newType
    }

    fun setTaxLevel(newLevel: Int?) {
        taxLevel = newLevel
    }

    fun setEffectiveDate(newDate: TaxEffectiveDates?) {
        effectiveDate = newDate
    }

    fun setEmployer(newEmployer: Employers?) {
        employer = newEmployer
    }

    fun setTaxType(newTaxType: TaxTypes?) {
        taxType = newTaxType
    }

    fun setTaxRule(newTaxRule: WorkTaxRules?) {
        taxRule = newTaxRule
    }

    fun getTaxTypeString(): String? {
        return taxTypeString
    }

    fun getTaxLevel(): Int? {
        return taxLevel
    }

    fun getEffectiveDate(): TaxEffectiveDates? {
        return effectiveDate
    }

    fun getEmployer(): Employers? {
        return employer
    }

    fun getTaxType(): TaxTypes? {
        return taxType
    }

    fun getTaxRule(): WorkTaxRules? {
        return taxRule
    }
}