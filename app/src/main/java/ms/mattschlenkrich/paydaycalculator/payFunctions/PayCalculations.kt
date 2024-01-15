package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.WorkDates

class PayCalculations(
    private val mainActivity: MainActivity,
    private val employer: Employers,
    private val cutOff: String,
    private val mView: View,
) {
    private val workDates = ArrayList<WorkDates>()

    init {
        getWorkDates()
    }

    private fun getWorkDates() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getWorkDateList(
                employer.employerId, cutOff
            ).observe(lifecycleOwner) { list ->
                workDates.clear()
                list.listIterator().forEach {
                    workDates.add(it)
                }
            }
        }
    }
}