package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxTypes

class MainViewModel(
    app: Application,
) : AndroidViewModel(app) {
    private var employer: Employers? = null
    private var taxType: WorkTaxTypes? = null

    fun setEmployer(newEmployer: Employers?) {
        employer = newEmployer
    }

    fun setTaxType(newTaxType: WorkTaxTypes?) {
        taxType = newTaxType
    }

    fun getEmployer(): Employers? {
        return employer
    }

    fun getTaxType(): WorkTaxTypes? {
        return taxType
    }

}