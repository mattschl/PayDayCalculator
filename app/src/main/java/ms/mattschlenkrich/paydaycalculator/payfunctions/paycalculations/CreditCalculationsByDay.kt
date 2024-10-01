package ms.mattschlenkrich.paydaycalculator.payfunctions.paycalculations

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import ms.mattschlenkrich.paydaycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDateExtraAndTypeAndDef
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class CreditCalculationsByDay(
    private val mainActivity: MainActivity,
    private val mView: View,
    private val currentPayPeriod: PayPeriods,
    private val workDateList: ArrayList<WorkDates>,
) {


    private fun getWorkDateExtras(): ArrayList<WorkDateExtraAndTypeAndDef> {
        val payPeriodExtras = ArrayList<WorkDateExtraAndTypeAndDef>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getWorkDateExtrasPerPay(
                currentPayPeriod.ppEmployerId,
                currentPayPeriod.ppCutoffDate
            ).observe(lifecycleOwner) { list ->
                payPeriodExtras.clear()
                list.listIterator().forEach {
                    payPeriodExtras.add(it)
                }
            }
        }
        return payPeriodExtras
    }

    private fun getExtraTypes(): ArrayList<WorkExtraTypes> {
        val extraTypes = ArrayList<WorkExtraTypes>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workExtraViewModel.getWorkExtraTypeList(
                currentPayPeriod.ppEmployerId
            ).observe(
                lifecycleOwner
            ) { list ->
                extraTypes.clear()
                list.listIterator().forEach {
                    extraTypes.add(it)
                }
            }
        }
        return extraTypes
    }
}