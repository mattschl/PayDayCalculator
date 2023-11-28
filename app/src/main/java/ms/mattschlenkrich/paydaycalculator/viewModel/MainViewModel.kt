package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paydaycalculator.model.Employers

class MainViewModel(
    app: Application,
) : AndroidViewModel(app) {
    private var employer: Employers? = null

    fun setEmployer(newEmployer: Employers?) {
        employer = newEmployer
    }

    fun getEmployer(): Employers? {
        return employer
    }

}