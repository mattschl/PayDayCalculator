package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDateExtraAndTypeFull
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class ExtraCalculations(
    private val mainActivity: MainActivity,
    private val mView: View,
    private val currentPayPeriod: PayPeriods,
) {
    private fun processExtrasPerDay(): ArrayList<WorkDateExtraAndTypeFull> {
        val workDateExtras = ArrayList<WorkDateExtraAndTypeFull>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getWorkDateExtrasPerPay(
                currentPayPeriod.ppEmployerId,
                currentPayPeriod.ppCutoffDate
            ).observe(lifecycleOwner) { list ->
                workDateExtras.clear()
                list.listIterator().forEach {
                    workDateExtras.add(it)
                }
            }
        }
        return workDateExtras
    }

}