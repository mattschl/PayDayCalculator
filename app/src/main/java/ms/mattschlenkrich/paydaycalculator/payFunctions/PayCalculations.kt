package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.util.Log
import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDates

private const val TAG = "PayCalculations"

class PayCalculations(
    private val mainActivity: MainActivity,
    private val employer: Employers,
    private val cutOff: String,
    private val mView: View,
) {
    private val workDates = ArrayList<WorkDates>()
    private val workExtras = ArrayList<WorkDateExtras>()
    private var rate = 0.0

    init {
        findWorkDates()
        findExtras()
        findRate()
    }

    fun getRegHours(): Double {
        var hours = 0.0
        for (day in workDates) {
            hours += day.wdRegHours
        }
        return hours
    }

    fun getRegPay(): Double {
        return getRegHours() * rate
    }

    fun getOtHours(): Double {
        var hours = 0.0
        for (day in workDates) {
            hours += day.wdOtHours
        }
        return hours
    }

    fun getOtPay(): Double {
        return getOtHours() * rate
    }

    fun getDblOtHours(): Double {
        var hours = 0.0
        for (day in workDates) {
            hours += day.wdDblOtHours
        }
        return hours
    }

    fun getDblOtPay(): Double {
        return getDblOtHours() * rate
    }

    fun getStatHours(): Double {
        var hours = 0.0
        for (day in workDates) {
            hours += day.wdStatHours
        }
        return hours
    }

    fun getStatPay(): Double {
        return getStatHours() * rate
    }

    fun getDailyExtraFixedTotal(): Double {
        var extraTotal = 0.0
        for (extra in workExtras) {
            if (extra.wdeIsFixed) {
                extraTotal += extra.wdeValue
            }
        }
        return extraTotal
    }

    fun getGrossPay(): Double {
        return getRegPay() + getOtPay() + getDblOtPay() + getStatPay() + getDailyExtraFixedTotal()
    }

    private fun findRate() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.employerViewModel.getEmployerPayRates(
                employer.employerId
            ).observe(lifecycleOwner) { rates ->
                rates.listIterator().forEach {
                    Log.d(
                        TAG, "RATE is ${it.eprPayRate} " +
                                "EFFECTIVE DATE is ${it.eprEffectiveDate}"
                    )
                    if (it.eprEffectiveDate <= cutOff) {
                        rate = it.eprPayRate
                    }
                }
            }
        }
    }

    private fun findWorkDates() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getWorkDateList(
                employer.employerId, cutOff
            ).observe(lifecycleOwner) { list ->
                workDates.clear()
                list.listIterator().forEach {
                    workDates.add(it)
                    Log.d(TAG, "ADDED DATE ${it.wdDate}")
                }
            }
        }
    }

    private fun findExtras() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getWorkDateExtrasPerPay(
                employer.employerId, cutOff
            ).observe(lifecycleOwner) { list ->
                workExtras.clear()
                list.listIterator().forEach {
                    workExtras.add(it)
                    Log.d(TAG, "EXTRA ADDED ${it.wdeName}")
                }
            }
        }
    }
}