package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paydaycalculator.repository.WorkExtraRepository

class WorkExtraViewModel(
    app: Application,
    private val workExtraRepository: WorkExtraRepository
) : AndroidViewModel(app) {

   
}