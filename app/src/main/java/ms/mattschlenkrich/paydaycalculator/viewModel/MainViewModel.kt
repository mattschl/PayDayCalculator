package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraFrequencies
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxRules
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxTypes

class MainViewModel(
    app: Application,
) : AndroidViewModel(app) {
    private var employer: Employers? = null
    private var taxType: WorkTaxTypes? = null
    private var extraFrequencyType: WorkExtraFrequencies? = null
    private var taxRule: WorkTaxRules? = null

    fun setEmployer(newEmployer: Employers?) {
        employer = newEmployer
    }

    fun setTaxType(newTaxType: WorkTaxTypes?) {
        taxType = newTaxType
    }

    fun setExtraFrequencyType(newExtraFrequencyType: WorkExtraFrequencies?) {
        extraFrequencyType = newExtraFrequencyType
    }

    fun setTaxRule(newTaxRule: WorkTaxRules?) {
        taxRule = newTaxRule
    }

    fun getEmployer(): Employers? {
        return employer
    }

    fun getTaxType(): WorkTaxTypes? {
        return taxType
    }

    fun getExtraFrequencyType(): WorkExtraFrequencies? {
        return extraFrequencyType
    }

    fun getTaxRule(): WorkTaxRules? {
        return taxRule
    }
}