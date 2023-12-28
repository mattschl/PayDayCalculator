package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxRules

class MainViewModel(
    app: Application,
) : AndroidViewModel(app) {
    private var employer: Employers? = null
    private var taxType: String? = null
    private var taxRule: WorkTaxRules? = null
    private var effectiveDate: String? = null
    private var taxLevel: Int? = null

    fun setTaxLevel(newLevel: Int?) {
        taxLevel = newLevel
    }

    fun setEffectiveDate(newDate: String?) {
        effectiveDate = newDate
    }

    fun setEmployer(newEmployer: Employers?) {
        employer = newEmployer
    }

    fun setTaxType(newTaxType: String?) {
        taxType = newTaxType
    }

    fun setTaxRule(newTaxRule: WorkTaxRules?) {
        taxRule = newTaxRule
    }

    fun getTaxLevel(): Int? {
        return taxLevel
    }

    fun getEffectiveDate(): String? {
        return effectiveDate
    }

    fun getEmployer(): Employers? {
        return employer
    }

    fun getTaxType(): String? {
        return taxType
    }

    fun getTaxRule(): WorkTaxRules? {
        return taxRule
    }
}