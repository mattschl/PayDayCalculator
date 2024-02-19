package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.util.Log
import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.ExtraTotal
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
    private var regHours = 0.0
    private var otHours = 0.0
    private var dblOtHours = 0.0
    private var statHours = 0.0
    private val extraTotals = ArrayList<ExtraTotal>()


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

    fun getRate() {
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

    fun getWorkDates() {
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

    fun getExtras() {
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