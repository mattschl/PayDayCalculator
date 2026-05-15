package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paycalculator.data.repository.PayDetailRepository

class PayDetailViewModel(
    app: Application,
    private val payDetailRepository: PayDetailRepository,
) : AndroidViewModel(app) {

    fun getHoursReg(employerId: Long, cutoffDate: String) =
        payDetailRepository.getHoursReg(employerId, cutoffDate)

    fun getHoursOt(employerId: Long, cutoffDate: String) =
        payDetailRepository.getHoursOt(employerId, cutoffDate)

    fun getHoursDblOt(employerId: Long, cutoffDate: String) =
        payDetailRepository.getHoursDblOt(employerId, cutoffDate)

    fun getHoursStat(employerId: Long, cutoffDate: String) =
        payDetailRepository.getHoursStat(employerId, cutoffDate)

    fun getDaysWorked(employerId: Long, cutoffDate: String) =
        payDetailRepository.getDaysWorked(employerId, cutoffDate)

    fun getPayRate(employerId: Long, cutoffDate: String) =
        payDetailRepository.getPayRate(employerId, cutoffDate)
}