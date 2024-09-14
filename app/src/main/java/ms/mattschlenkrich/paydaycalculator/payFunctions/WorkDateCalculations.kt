package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class WorkDateCalculations(
    private val mainActivity: MainActivity,
    private val employerId: Long,
    private val mView: View,
    private val currentPayPeriod: PayPeriods
) {
    private val workDateList = processWorkDates()
    private var daysWorked = 0
    private var hoursReg = 0.0
    private var hoursOt = 0.0
    private var hoursDblOt = 0.0
    private var hoursStat = 0.0

    fun getWorkDateList(): ArrayList<WorkDates> {
        return workDateList
    }

    private fun processWorkDates(): ArrayList<WorkDates> {
        val workDates = ArrayList<WorkDates>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getWorkDateList(
                employerId, currentPayPeriod.ppCutoffDate
            ).observe(lifecycleOwner) { list ->
                workDates.clear()
                hoursReg = 0.0
                hoursOt = 0.0
                hoursDblOt = 0.0
                hoursStat = 0.0
                daysWorked = 0
                list.listIterator().forEach {
                    workDates.add(it)
                    hoursReg += it.wdRegHours
                    hoursOt += it.wdOtHours
                    hoursDblOt += it.wdDblOtHours
                    hoursStat += it.wdStatHours
                    if (it.wdRegHours != 0.0 ||
                        it.wdOtHours != 0.0 ||
                        it.wdDblOtHours != 0.0
                    ) {
                        daysWorked++
                    }
                }
            }
        }
        return workDates
    }
}
