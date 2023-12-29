package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.TaxEffectiveDates
import ms.mattschlenkrich.paydaycalculator.model.TaxTypes
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxRules

class MainViewModel(
    app: Application,
) : AndroidViewModel(app) {
    private var employer: Employers? = null
    private var taxType: TaxTypes? = null
    private var taxTypeString: String? = null
    private var taxRule: WorkTaxRules? = null
    private var effectiveDate: TaxEffectiveDates? = null
    private var effectiveDateString: String? = null
    private var taxLevel: Int? = null
    private var callingFragment: String? = null

    fun setCallingFragment(newFragment: String?) {
        callingFragment = newFragment
    }

    fun addCallingFragment(newFragment: String) {
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