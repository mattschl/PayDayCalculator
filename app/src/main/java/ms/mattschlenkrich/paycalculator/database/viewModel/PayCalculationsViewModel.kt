package ms.mattschlenkrich.paycalculator.database.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paycalculator.database.repository.PayCalculationsRepository

class PayCalculationsViewModel(
    app: Application, private val payCalculationsRepository: PayCalculationsRepository
) : AndroidViewModel(app) {
    fun getPayRate(employerId: Long, cutoffDate: String) =
        payCalculationsRepository.getPayRate(employerId, cutoffDate)

    fun getWorkDateList(employerId: Long, cutOff: String) =
        payCalculationsRepository.getWorkDateList(employerId, cutOff)

    fun getWorkDateExtrasPerPay(employerId: Long, cutOff: String) =
        payCalculationsRepository.getWorkDateExtrasPerPay(employerId, cutOff)

    fun getExtraTypesAndCurrentDef(employerId: Long, cutoffDate: String, appliesTo: Int) =
        payCalculationsRepository.getExtraTypesAndCurrentDef(
            employerId, cutoffDate, appliesTo
        )
}