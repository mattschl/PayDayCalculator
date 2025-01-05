package ms.mattschlenkrich.paycalculator.payfunctions.paycalculations

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraAndTotal
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paycalculator.database.model.payperiod.PayPeriodHourlySummary
import ms.mattschlenkrich.paycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class CreditCalculationsFinal(
    private val mainActivity: MainActivity,
    private val mView: View,
    private val currentPayPeriod: PayPeriods,
    private val hourlySummary: PayPeriodHourlySummary,
    private val creditCalculations: CreditCalculationsInitial
) {
    fun getExtraList(): List<ExtraAndTotal> {
        val extraList = ArrayList<ExtraAndTotal>()
        for (extra in getExtrasByPercent()) {
            val total =
                extra.definition.weValue *
                        (creditCalculations.getCreditTotal() +
                                hourlySummary.payRate *
                                (hourlySummary.hoursReg +
                                        hourlySummary.hoursOt +
                                        hourlySummary.hoursDblOt +
                                        hourlySummary.hoursStat)) / 100
            extraList.add(
                ExtraAndTotal(
                    extra.extraType.wetName,
                    total
                )
            )
        }

        return extraList
    }


    private fun getExtrasByPercent(): List<ExtraDefinitionAndType> {
        val extraList = ArrayList<ExtraDefinitionAndType>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workExtraViewModel.getExtraTypesAndDef(
                currentPayPeriod.ppEmployerId,
                currentPayPeriod.ppCutoffDate,
                4
            ).observe(lifecycleOwner) { list ->
                extraList.clear()
                list.listIterator().forEach {
                    extraList.add(it)
                }
            }
        }
        return extraList
    }
}